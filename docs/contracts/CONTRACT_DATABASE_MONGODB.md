# Contrato de Persistencia NoSQL (MongoDB) — Sonido Vivo
**Fase DAG:** P2 — Contratos & Schemas (`[[Agente CDD]]` + `[[Agente JSON Schema]]`)  
**Colección Primaria:** `productos`  
**Base de Datos:** `sonido_vivo_nosql`  
**Capa de Arquitectura:** `infrastructure/adapter/out/persistence/mongo` y `domain/port/out`  

---

## 1. Esquema Formal de Validación en Base de Datos (`$jsonSchema`)

Este esquema se aplica a nivel de motor en MongoDB para garantizar integridad estructural y tipos estrictos antes de cualquier inserción o actualización:

```json
{
  "$jsonSchema": {
    "bsonType": "object",
    "required": ["codigo", "nombre", "categoria", "precio_clp", "stock_disponible", "estado"],
    "properties": {
      "codigo": {
        "bsonType": "string",
        "pattern": "^[A-Z]{1,4}-[A-Z0-9]{2,5}-[A-Z0-9]{2,10}-[0-9]{2}$",
        "description": "Código de inventario normalizado (ej: G-FND-STRAT-01). Requerido y único."
      },
      "nombre": {
        "bsonType": "string",
        "minLength": 3,
        "maxLength": 150,
        "description": "Nombre comercial del producto."
      },
      "categoria": {
        "enum": ["Cuerdas", "Audio Pro", "Percusion", "Teclados", "Vientos", "Accesorios"],
        "description": "Categoría principal homologada."
      },
      "precio_clp": {
        "bsonType": "int",
        "minimum": 1000,
        "maximum": 50000000,
        "description": "Precio unitario en CLP (entero sin decimales)."
      },
      "stock_disponible": {
        "bsonType": "int",
        "minimum": 0,
        "description": "Existencias en bodega. Invariante: nunca menor a 0."
      },
      "estado": {
        "enum": ["ACTIVO", "AGOTADO", "DESCONTINUADO"],
        "description": "Estado del ciclo de vida para borrado lógico."
      },
      "especificaciones": {
        "bsonType": "object",
        "description": "Subdocumento polimórfico con atributos dinámicos según el tipo de instrumento."
      },
      "tags": {
        "bsonType": "array",
        "uniqueItems": true,
        "items": { "bsonType": "string" },
        "description": "Etiquetas de categorización y búsqueda."
      },
      "resenas": {
        "bsonType": "array",
        "items": {
          "bsonType": "object",
          "required": ["cliente", "calificacion", "fecha"],
          "properties": {
            "cliente": { "bsonType": "string", "minLength": 2 },
            "calificacion": { "bsonType": "int", "minimum": 1, "maximum": 5 },
            "comentario": { "bsonType": "string", "maxLength": 500 },
            "fecha": { "bsonType": "date" }
          }
        }
      },
      "ultima_venta": { "bsonType": "date" },
      "fecha_baja": { "bsonType": "date" }
    }
  }
}
```

---

## 2. Contrato de Índices de Persistencia

| Nombre del Índice | Claves / Campos | Propiedades | Propósito de Rendimiento |
| :--- | :--- | :--- | :--- |
| `idx_codigo_unique` | `{ "codigo": 1 }` | `unique: true` | Garantiza unicidad del SKU e indexación O(1). |
| `idx_cat_precio` | `{ "categoria": 1, "precio_clp": 1 }` | Compuesto | Búsquedas por catálogo y ordenamiento de precios. |
| `idx_tags` | `{ "tags": 1 }` | Multikey | Búsquedas de marketing y etiquetas. |
| `idx_espec_tipo` | `{ "especificaciones.tipo": 1 }` | Sparse | Filtro por tipo polimórfico de instrumento. |

---

## 3. Puertos de Salida en Dominio Puro (`domain/port/out`)

En Arquitectura Hexagonal, el dominio define estas interfaces en Java 21 sin ninguna anotación de Spring ni dependencias de MongoDB:

```java
package cl.duoc.sonidovivo.domain.port.out;

import cl.duoc.sonidovivo.domain.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findByCode(String code);
    List<Product> findAllPaged(int page, int size, String category, String status);
    boolean decrementStockAtomic(String code, int quantity);
    void addReview(String code, String customer, int rating, String comment);
    void updateStatus(String code, String status);
    int applyBatchPriceAdjustment(double percentage, String category);
}
```

```java
package cl.duoc.sonidovivo.domain.port.out;

import java.util.List;

public interface DashboardMetricsPort {
    CategoryInventoryMetric getCategoryMetrics();
    
    record CategoryMetric(String category, int totalUnits, long totalValueClp, double averagePrice) {}
    record CategoryInventoryMetric(long totalWarehouseValueClp, int totalUnitsCount, List<CategoryMetric> categories) {}
}
```
