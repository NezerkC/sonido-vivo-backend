# Arquitectura de Backend para Persistencia NoSQL y Relacional
**Asignatura / Contexto:** Taller de Base de Datos (BDY1103) — Unidad 3  
**Dominio:** Sonido Vivo (E-commerce de Audio Profesional e Instrumentos)  
**Motor Primario:** MongoDB (`sonido_vivo_nosql`)  
**Motor Transaccional Coexistente:** Oracle Database / PostgreSQL  
**Mecanismo de Abstracción:** Spring Data MongoDB / Hexagonal Outbound Adapters  

---

## 1. Visión General y Coexistencia Políglota

El backend en **Spring Boot 3** implementa una arquitectura desacoplada donde el Dominio puro no tiene conocimiento de los mecanismos de almacenamiento. La capa de infraestructura (`infrastructure/adapter/out/persistence/mongo`) actúa como un adaptador de salida que traduce las entidades de negocio a documentos BSON.

### Coexistencia de Paradigmas:
1. **MongoDB (Documental NoSQL - Unidad 3):**
   - Catálogo de productos con atributos técnicos polimórficos (`especificaciones`).
   - Comentarios y valoraciones embebidas de clientes (`resenas`).
   - Métricas analíticas y agregaciones en tiempo real (`$aggregate`).
   - Logs de eventos y auditoría operativa.
2. **Motor Relacional (SQL / ACID):**
   - Registro formal de ventas cerradas, boletas, facturas y liquidaciones transaccionales estrictas.

---

## 2. Esquema BSON y Modelado Documental (`productos`)

La colección `productos` se gobierna mediante validación estricta en base de datos (`$jsonSchema`) y se mapea en Java a través de documentos `@Document`.

### 2.1 Mapeo de Documento en Spring Data (`MongoProductDocument.java`)
```java
package cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "productos")
@CompoundIndex(name = "cat_precio_idx", def = "{'categoria': 1, 'precio_clp': 1}")
public class MongoProductDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String codigo;

    private String nombre;
    private String categoria;

    @Field("precio_clp")
    private Integer precioClp;

    @Field("stock_disponible")
    private Integer stockDisponible;

    private String estado; // ACTIVO, AGOTADO, DESCONTINUADO

    // Polimorfismo instrumental: almacena pares clave-valor dinámicos
    private Map<String, Object> especificaciones;

    private List<String> tags;

    // Subdocumentos embebidos
    private List<ResenaEmbeddable> resenas;

    @Field("ultima_venta")
    private Instant ultimaVenta;

    @Field("fecha_baja")
    private Instant fechaBaja;

    // Getters, Setters y Constructores
}
```

### 2.2 Subdocumento Embebido (`ResenaEmbeddable.java`)
```java
package cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.mongo.document;

import java.time.Instant;

public class ResenaEmbeddable {
    private String cliente;
    private Integer calificacion; // 1 a 5
    private String comentario;
    private Instant fecha;

    // Getters, Setters y Constructores
}
```

---

## 3. Implementación de Consultas y Operaciones Atómicas

### 3.1 Pipeline de Agregación para Dashboard (`$aggregate`)
En lugar de traer miles de registros a memoria, el backend delega el cálculo al motor de MongoDB mediante `MongoTemplate`.

**Consulta Nativa:**
```javascript
db.productos.aggregate([
  { $match: { estado: "ACTIVO" } },
  {
    $group: {
      _id: "$categoria",
      total_unidades: { $sum: "$stock_disponible" },
      valor_total_inventario: { $sum: { $multiply: ["$precio_clp", "$stock_disponible"] } },
      precio_promedio: { $avg: "$precio_clp" }
    }
  },
  { $sort: { valor_total_inventario: -1 } }
]);
```

**Implementación en Spring Boot (`MongoMetricsRepositoryAdapter.java`):**
```java
@Service
public class MongoMetricsRepositoryAdapter implements DashboardMetricsPort {

    private final MongoTemplate mongoTemplate;

    public MongoMetricsRepositoryAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<CategoriaMetrica> obtenerInventarioValorizado() {
        MatchOperation match = Aggregation.match(Criteria.where("estado").is("ACTIVO"));
        
        GroupOperation group = Aggregation.group("categoria")
            .sum("stock_disponible").as("totalUnidades")
            .sum(ArithmeticOperators.Multiply.valueOf("precio_clp").multiplyBy("stock_disponible")).as("valorTotalInventario")
            .avg("precio_clp").as("precioPromedio");
            
        SortOperation sort = Aggregation.sort(Sort.Direction.DESC, "valorTotalInventario");

        Aggregation aggregation = Aggregation.newAggregation(match, group, sort);
        AggregationResults<CategoriaMetrica> results = mongoTemplate.aggregate(aggregation, "productos", CategoriaMetrica.class);
        
        return results.getMappedResults();
    }
}
```

### 3.2 Operaciones Atómicas en MongoDB

1. **Decremento e Incremento Atómico de Stock (`$inc`):**
   Garantiza que dos compras simultáneas no generen condiciones de carrera (*race conditions*).
   ```java
   Query query = new Query(Criteria.where("codigo").is(codigo).and("stock_disponible").gte(cantidad));
   Update update = new Update()
       .inc("stock_disponible", -cantidad)
       .set("ultima_venta", Instant.now());
   
   UpdateResult result = mongoTemplate.updateFirst(query, update, MongoProductDocument.class);
   if (result.getModifiedCount() == 0) {
       throw new InsufficientStockException("Stock insuficiente para el producto: " + codigo);
   }
   ```

2. **Inserción de Reseñas en Array Embebido (`$push`):**
   ```java
   Query query = new Query(Criteria.where("codigo").is(codigo));
   Update update = new Update().push("resenas", nuevaResena);
   mongoTemplate.updateFirst(query, update, MongoProductDocument.class);
   ```

3. **Borrado Lógico y Fecha de Baja (`$set`):**
   ```java
   Query query = new Query(Criteria.where("codigo").is(codigo));
   Update update = new Update()
       .set("estado", "DESCONTINUADO")
       .set("fecha_baja", Instant.now());
   mongoTemplate.updateFirst(query, update, MongoProductDocument.class);
   ```

---

## 4. Equivalencia de Procedimientos Almacenados (PL/SQL a Java)

| Procedimiento Original (Oracle) | Implementación Backend (Spring Boot + NoSQL) | Comportamiento |
| :--- | :--- | :--- |
| `prc_calcula_precio(p_porc)` | `POST /api/v1/admin/operations/price-adjustment` | Actualiza masivamente los precios con `Update.multiply` o cálculo en lote, respetando margen mínimo y redondeo en pesos chilenos. |
| `fun_valida_email(p_email)` | `CustomerEmailValidator` (Jakarta Validator) | Validación regex estándar RFC 5322 antes de persistir o en el binding HTTP. |
| Triggers de Auditoría | Interceptores Spring Data (`AbstractMongoEventListener` o `@LastModifiedDate`) | Audita quién y cuándo modificó cada documento sin sobrecargar la lógica de negocio. |

---

## 5. Configuración de Conexión (`application.yml`)

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/sonido_vivo_nosql
      auto-index-creation: true
```
