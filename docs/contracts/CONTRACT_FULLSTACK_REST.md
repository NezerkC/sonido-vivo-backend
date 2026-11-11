# Contrato de Integración REST & OpenAPI 3.0 — Sonido Vivo
**Fase DAG:** P2 — Contratos & Schemas (`[[Agente OpenAPI]]` + `[[Agente Runtime Validation]]`)  
**Servidor:** Spring Boot 3.3+ (Java 21 LTS)  
**Cliente:** React (Vite / TypeScript)  
**Protocolo:** HTTP/1.1 RESTful JSON  
**Base URL:** `http://localhost:8080/api/v1`  

---

## 1. Contratos DTO con Validación en Runtime (Backend - Java Jakarta)

Los DTOs garantizan que ningún dato corrupto ingrese a los casos de uso del dominio:

### 1.1 `ProductCreateRequestDTO.java`
```java
package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

public record ProductCreateRequestDTO(
    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "^[A-Z]{1,4}-[A-Z0-9]{2,5}-[A-Z0-9]{2,10}-[0-9]{2}$", 
             message = "Formato de código inválido (ej: G-FND-STRAT-01)")
    String codigo,

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    String nombre,

    @NotBlank(message = "La categoría es obligatoria")
    @Pattern(regexp = "^(Cuerdas|Audio Pro|Percusion|Teclados|Vientos|Accesorios)$", 
             message = "Categoría no homologada")
    String categoria,

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 1000, message = "El precio mínimo es $1.000 CLP")
    Integer precioClp,

    @NotNull(message = "El stock inicial es obligatorio")
    @Min(value = 0, message = "El stock inicial no puede ser negativo")
    Integer stockDisponible,

    Map<String, Object> especificaciones,
    List<String> tags
) {}
```

### 1.2 `StockAdjustmentDTO.java`
```java
package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.*;

public record StockAdjustmentDTO(
    @NotNull(message = "El delta de ajuste es obligatorio")
    Integer delta, // Negativo para ventas, positivo para reposición

    @NotBlank(message = "El motivo de ajuste es obligatorio")
    @Pattern(regexp = "^(VENTA_DIRECTA_MOSTRADOR|DEVOLUCION_CLIENTE|MERMA_AVERIA|INGRESO_PROVEEDOR)$", 
             message = "Motivo no admitido")
    String motivo
) {}
```

### 1.3 `PriceAdjustmentDTO.java` (Equivalente `prc_calcula_precio`)
```java
package cl.duoc.sonidovivo.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.*;

public record PriceAdjustmentDTO(
    @NotNull(message = "El porcentaje es obligatorio")
    @DecimalMin(value = "-50.0", message = "El descuento no puede exceder el 50%")
    @DecimalMax(value = "100.0", message = "El incremento no puede exceder el 100%")
    Double porcentaje,

    String categoria, // Opcional: si es null, aplica a todo el inventario

    @NotBlank(message = "Se requiere una justificación para auditoría")
    String motivo
) {}
```

---

## 2. Contratos de Tipado para Frontend (React / TypeScript)

En el frontend React (`src/types/api.d.ts`), estos contratos evitan errores de tipeo y desacoplan la UI:

```typescript
export type CategoriaInstrumento = 
  | 'Cuerdas' 
  | 'Audio Pro' 
  | 'Percusion' 
  | 'Teclados' 
  | 'Vientos' 
  | 'Accesorios';

export type EstadoProducto = 'ACTIVO' | 'AGOTADO' | 'DESCONTINUADO';

export interface Resena {
  cliente: string;
  calificacion: number; // 1 - 5
  comentario: string;
  fecha: string;
}

export interface Producto {
  codigo: string;
  nombre: string;
  categoria: CategoriaInstrumento;
  precio_clp: number;
  stock_disponible: number;
  estado: EstadoProducto;
  especificaciones?: Record<string, any>;
  tags?: string[];
  resenas?: Resena[];
}

export interface MetricaCategoria {
  categoria: string;
  total_unidades: number;
  valor_total_inventario: number;
  precio_promedio: number;
}

export interface DashboardMetricsResponse {
  kpis: {
    capitalTotalEnBodega: number;
    unidadesTotales: number;
    categoriasActivas: number;
  };
  desglosePorCategoria: MetricaCategoria[];
}

export interface ReajustePrecioRequest {
  porcentaje: number;
  categoria?: string;
  motivo: string;
}
```

---

## 3. Especificación OpenAPI 3.0 (Endpoints del Dashboard)

```yaml
openapi: 3.0.3
info:
  title: Sonido Vivo REST API
  description: API Backend desacoplada para gestión de E-commerce y Dashboard Administrativo
  version: 1.0.0
servers:
  - url: http://localhost:8080/api/v1
paths:
  /auth/login:
    post:
      summary: Iniciar sesión de administrador
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [username, password]
              properties:
                username: { type: string }
                password: { type: string }
      responses:
        '200':
          description: Autenticación exitosa con JWT
        '401':
          description: Credenciales inválidas

  /admin/dashboard/metrics:
    get:
      summary: Obtener métricas agregadas del inventario (NoSQL Pipeline)
      security:
        - BearerAuth: []
      responses:
        '200':
          description: Métricas calculadas en tiempo real por categoría

  /admin/products:
    get:
      summary: Catálogo paginado de productos
      parameters:
        - name: page
          in: query
          schema: { type: integer, default: 0 }
        - name: size
          in: query
          schema: { type: integer, default: 10 }
        - name: categoria
          in: query
          schema: { type: string }
      responses:
        '200':
          description: Lista paginada de productos
    post:
      summary: Crear nuevo producto con esquema polimórfico
      security:
        - BearerAuth: []
      responses:
        '201':
          description: Producto creado con éxito
        '400':
          description: Error en validación de DTO

  /admin/products/{codigo}/stock:
    patch:
      summary: Ajuste atómico de inventario ($inc)
      parameters:
        - name: codigo
          in: path
          required: true
          schema: { type: string }
      responses:
        '200':
          description: Stock actualizado atómicamente

  /admin/operations/price-adjustment:
    post:
      summary: Reajuste porcentual masivo de precios (prc_calcula_precio)
      security:
        - BearerAuth: []
      responses:
        '200':
          description: Lote de productos actualizado con éxito

components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
```
