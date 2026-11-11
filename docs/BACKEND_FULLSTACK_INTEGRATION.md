# Arquitectura de Backend para Integración Fullstack
**Asignatura / Contexto:** Desarrollo Fullstack II (DSY1104) — Unidad 3  
**Dominio:** Sonido Vivo (E-commerce de Audio Profesional e Instrumentos)  
**Frontend:** React (Vite) + React Router + Bootstrap / CSS  
**Backend:** Spring Boot 3.3+ (Java 21 LTS, Maven)  
**Protocolo:** RESTful HTTP sobre JSON  
**Seguridad:** Spring Security + JWT (Tokens Stateless)  

---

## 1. Visión General de la Integración Frontend-Backend

Para cumplir con los resultados de aprendizaje de la **Unidad 3 de Fullstack II**, este documento especifica cómo la aplicación cliente en **React** se comunica de forma segura y reactiva con la API REST construida en **Spring Boot**.

### Principios Arquitectónicos:
1. **Desacoplamiento Estricto:** React jamás interactúa directamente con la persistencia ni conoce los detalles de MongoDB o SQL; consume exclusivamente contratos JSON.
2. **Seguridad y Rutas Protegidas:** La zona administrativa (`/admin`) queda blindada. Solo usuarios con credenciales válidas y rol `ROLE_ADMIN` pueden obtener y enviar información sensible.
3. **Manejo Centralizado de Errores:** Respuestas de error uniformes mediante `@RestControllerAdvice` con códigos HTTP estándar (`400`, `401`, `403`, `404`, `409`, `500`).

---

## 2. Flujo de Autenticación y Guardia de Rutas

```
[Usuario] 
   │
   ▼
[React: /login] ── POST /api/v1/auth/login ──► [Spring Boot: AuthController]
   │                                                    │ Valida BCrypt
   ◄──────── Retorna Token JWT (200 OK) ───────────────┘
   │
   ▼ (Guarda Token en Memory / LocalStorage)
[React: ProtectedRoute <ROLE_ADMIN>]
   │
   ├── Valida existencia de Token
   ├── Decodifica Claims / Expiración
   └── Permite acceso a /admin o Redirige a /login
```

### 2.1 Componente de Ruta Protegida en React (`ProtectedRoute.jsx`)
```jsx
import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

export const ProtectedRoute = ({ allowedRoles = ['ROLE_ADMIN'] }) => {
  const token = localStorage.getItem('token');
  const user = JSON.parse(localStorage.getItem('usuario') || '{}');

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles.length > 0 && !allowedRoles.includes(user.rol)) {
    return <Navigate to="/" replace />; // Redirige a la tienda pública si no tiene permisos
  }

  return <Outlet />;
};
```

---

## 3. Configuración de CORS en Spring Boot (`CorsConfig.java`)

Para que el servidor de desarrollo de React en Vite (`http://localhost:5173`) pueda consumir los servicios sin bloqueos del navegador por política de mismo origen (*Same-Origin Policy*):

```java
package cl.duoc.sonidovivo.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        config.setAllowedHeaders(List.of("Origin", "Content-Type", "Accept", "Authorization"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/**", config);
        return new CorsFilter(source);
    }
}
```

---

## 4. Consumo de Servicios REST desde React

Se define un cliente HTTP centralizado con interceptor para adjuntar el encabezado `Authorization: Bearer <token>` de forma automática.

### 4.1 Cliente API Base (`src/services/api.js`)
```javascript
const BASE_URL = 'http://localhost:8080/api/v1';

export const apiClient = async (endpoint, options = {}) => {
  const token = localStorage.getItem('token');
  
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.message || `Error HTTP: ${response.status}`);
  }

  if (response.status === 204) return null;
  return response.json();
};
```

### 4.2 Servicio de Métricas y Catálogo (`src/services/adminService.js`)
```javascript
import { apiClient } from './api';

export const adminService = {
  // Obtiene KPIs y distribución de capital para gráficos del Dashboard
  getMetrics: () => apiClient('/admin/dashboard/metrics'),

  // Lista de productos con paginación y filtros
  getProducts: (params = '') => apiClient(`/admin/products${params}`),

  // Creación de producto
  createProduct: (productData) => apiClient('/admin/products', {
    method: 'POST',
    body: JSON.stringify(productData),
  }),

  // Reajuste porcentual masivo de precios
  applyPriceAdjustment: (adjustmentData) => apiClient('/admin/operations/price-adjustment', {
    method: 'POST',
    body: JSON.stringify(adjustmentData),
  }),

  // Ajuste atómico de inventario
  updateStock: (codigo, delta, motivo) => apiClient(`/admin/products/${codigo}/stock`, {
    method: 'PATCH',
    body: JSON.stringify({ delta, motivo }),
  }),

  // Baja lógica
  changeProductStatus: (codigo, estado) => apiClient(`/admin/products/${codigo}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ estado }),
  }),
};
```

---

## 5. Integración del Dashboard en React (`AdminDashboard.jsx`)

El componente principal del panel de administración consume en su ciclo de vida (`useEffect`) el pipeline de métricas y renderiza:
1. **Tarjetas de KPI:** Capital total en bodega, unidades totales y categorías activas.
2. **Tabla Interactiva:** Productos con stock crítico y opciones de ajuste rápido.
3. **Modal de Reajuste Masivo:** Formulario para ingresar el porcentaje de variación de precios y disparar la acción al backend.

```jsx
import React, { useEffect, useState } from 'react';
import { adminService } from '../services/adminService';

export const AdminDashboard = () => {
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    adminService.getMetrics()
      .then(data => setMetrics(data))
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="spinner">Cargando métricas...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;

  return (
    <div className="admin-container">
      <h2>Panel de Control - Sonido Vivo</h2>
      
      {/* Tarjetas KPI */}
      <div className="kpi-grid">
        <div className="card">
          <h4>Capital en Bodega</h4>
          <p className="kpi-value">${metrics.kpis.capitalTotalEnBodega.toLocaleString('es-CL')}</p>
        </div>
        <div className="card">
          <h4>Unidades Totales</h4>
          <p className="kpi-value">{metrics.kpis.unidadesTotales} unidades</p>
        </div>
        <div className="card">
          <h4>Familias de Instrumentos</h4>
          <p className="kpi-value">{metrics.kpis.categoriasActivas}</p>
        </div>
      </div>

      {/* Tabla Desglose por Categoría */}
      <table className="table mt-4">
        <thead>
          <tr>
            <th>Categoría</th>
            <th>Stock Total</th>
            <th>Valorización</th>
            <th>Precio Promedio</th>
          </tr>
        </thead>
        <tbody>
          {metrics.desglosePorCategoria.map((cat) => (
            <tr key={cat.categoria}>
              <td>{cat.categoria}</td>
              <td>{cat.total_unidades}</td>
              <td>${cat.valor_total_inventario.toLocaleString('es-CL')}</td>
              <td>${Math.round(cat.precio_promedio).toLocaleString('es-CL')}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
```
