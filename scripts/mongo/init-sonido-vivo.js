/**
 * Sonido Vivo - MongoDB Initialization Script
 * Database: sonido_vivo_nosql
 * Collection: productos
 * Contract Reference: docs/contracts/CONTRACT_DATABASE_MONGODB.md
 * Task: T08
 */

const dbName = 'sonido_vivo_nosql';
const targetDb = db.getSiblingDB(dbName);

print(`[MongoDB Init] Initializing database: ${dbName}`);

// 1. Create collection with strict $jsonSchema validator
const collections = targetDb.getCollectionNames();
if (!collections.includes('productos')) {
  print('[MongoDB Init] Creating collection "productos" with $jsonSchema validation...');
  targetDb.createCollection('productos', {
    validator: {
      $jsonSchema: {
        bsonType: 'object',
        required: ['codigo', 'nombre', 'categoria', 'precio_clp', 'stock_disponible', 'estado'],
        properties: {
          codigo: {
            bsonType: 'string',
            pattern: '^[A-Z]{1,4}-[A-Z0-9]{2,5}-[A-Z0-9]{2,10}-[0-9]{2}$',
            description: 'Código de inventario normalizado (ej: G-FND-STRAT-01). Requerido y único.'
          },
          nombre: {
            bsonType: 'string',
            minLength: 3,
            maxLength: 150,
            description: 'Nombre comercial del producto.'
          },
          categoria: {
            enum: ['Cuerdas', 'Audio Pro', 'Percusion', 'Teclados', 'Vientos', 'Accesorios'],
            description: 'Categoría principal homologada.'
          },
          precio_clp: {
            bsonType: 'int',
            minimum: 1000,
            maximum: 50000000,
            description: 'Precio unitario en CLP (entero sin decimales).'
          },
          stock_disponible: {
            bsonType: 'int',
            minimum: 0,
            description: 'Existencias en bodega. Invariante: nunca menor a 0.'
          },
          estado: {
            enum: ['ACTIVO', 'AGOTADO', 'DESCONTINUADO'],
            description: 'Estado del ciclo de vida para borrado lógico.'
          },
          especificaciones: {
            bsonType: 'object',
            description: 'Subdocumento polimórfico con atributos dinámicos según el tipo de instrumento.'
          },
          tags: {
            bsonType: 'array',
            uniqueItems: true,
            items: { bsonType: 'string' },
            description: 'Etiquetas de categorización y búsqueda.'
          },
          resenas: {
            bsonType: 'array',
            items: {
              bsonType: 'object',
              required: ['cliente', 'calificacion', 'fecha'],
              properties: {
                cliente: { bsonType: 'string', minLength: 2 },
                calificacion: { bsonType: 'int', minimum: 1, maximum: 5 },
                comentario: { bsonType: 'string', maxLength: 500 },
                fecha: { bsonType: 'date' }
              }
            }
          },
          ultima_venta: { bsonType: 'date' },
          fecha_baja: { bsonType: 'date' }
        }
      }
    },
    validationLevel: 'strict',
    validationAction: 'error'
  });
} else {
  print('[MongoDB Init] Collection "productos" already exists. Updating validator...');
  targetDb.runCommand({
    collMod: 'productos',
    validator: {
      $jsonSchema: {
        bsonType: 'object',
        required: ['codigo', 'nombre', 'categoria', 'precio_clp', 'stock_disponible', 'estado'],
        properties: {
          codigo: {
            bsonType: 'string',
            pattern: '^[A-Z]{1,4}-[A-Z0-9]{2,5}-[A-Z0-9]{2,10}-[0-9]{2}$',
            description: 'Código de inventario normalizado.'
          },
          nombre: {
            bsonType: 'string',
            minLength: 3,
            maxLength: 150
          },
          categoria: {
            enum: ['Cuerdas', 'Audio Pro', 'Percusion', 'Teclados', 'Vientos', 'Accesorios']
          },
          precio_clp: {
            bsonType: 'int',
            minimum: 1000,
            maximum: 50000000
          },
          stock_disponible: {
            bsonType: 'int',
            minimum: 0
          },
          estado: {
            enum: ['ACTIVO', 'AGOTADO', 'DESCONTINUADO']
          },
          especificaciones: {
            bsonType: 'object'
          },
          tags: {
            bsonType: 'array',
            uniqueItems: true,
            items: { bsonType: 'string' }
          },
          resenas: {
            bsonType: 'array'
          },
          ultima_venta: { bsonType: 'date' },
          fecha_baja: { bsonType: 'date' }
        }
      }
    }
  });
}

// 2. Create the 4 required indexes
print('[MongoDB Init] Creating indexes...');
targetDb.productos.createIndex(
  { codigo: 1 },
  { unique: true, name: 'idx_codigo_unique' }
);

targetDb.productos.createIndex(
  { categoria: 1, precio_clp: 1 },
  { name: 'idx_cat_precio' }
);

targetDb.productos.createIndex(
  { tags: 1 },
  { name: 'idx_tags' }
);

targetDb.productos.createIndex(
  { 'especificaciones.tipo': 1 },
  { name: 'idx_espec_tipo' }
);

// 3. Seed initial products if collection is empty
const count = targetDb.productos.countDocuments();
if (count === 0) {
  print('[MongoDB Init] Seeding baseline inventory...');
  targetDb.productos.insertMany([
    {
      codigo: 'G-FND-STRAT-01',
      nombre: 'Fender Player Stratocaster Polar White',
      categoria: 'Cuerdas',
      precio_clp: NumberInt(849990),
      stock_disponible: NumberInt(12),
      estado: 'ACTIVO',
      especificaciones: {
        tipo: 'Electrica',
        madera_cuerpo: 'Aliso',
        capsulas: 'SSS Player Series',
        trastes: NumberInt(22)
      },
      tags: ['guitarra', 'electrica', 'fender', 'stratocaster'],
      resenas: [
        {
          cliente: 'Claudio Parra',
          calificacion: NumberInt(5),
          comentario: 'Excelente respuesta tonal y mástil muy suave.',
          fecha: new Date('2026-02-15T14:30:00Z')
        }
      ]
    },
    {
      codigo: 'MIC-SHU-SM58-01',
      nombre: 'Shure SM58 Microfono Dinamico Cardioide',
      categoria: 'Audio Pro',
      precio_clp: NumberInt(119990),
      stock_disponible: NumberInt(35),
      estado: 'ACTIVO',
      especificaciones: {
        tipo: 'Dinamico',
        patron_polar: 'Cardioide',
        respuesta_frecuencia: '50Hz - 15kHz',
        impedancia_ohms: NumberInt(300)
      },
      tags: ['microfono', 'voz', 'shure', 'audio pro'],
      resenas: []
    },
    {
      codigo: 'K-YAM-P45-01',
      nombre: 'Yamaha P-45 Piano Digital 88 Teclas Pesadas',
      categoria: 'Teclados',
      precio_clp: NumberInt(499990),
      stock_disponible: NumberInt(8),
      estado: 'ACTIVO',
      especificaciones: {
        tipo: 'Piano Digital',
        teclas: NumberInt(88),
        mecanismo: 'GHS Ponderado',
        polifonia: NumberInt(64)
      },
      tags: ['teclado', 'piano', 'yamaha', '88-teclas'],
      resenas: []
    },
    {
      codigo: 'D-ROL-TD02-01',
      nombre: 'Roland TD-02KV Bateria Electronica V-Drums',
      categoria: 'Percusion',
      precio_clp: NumberInt(589990),
      stock_disponible: NumberInt(5),
      estado: 'ACTIVO',
      especificaciones: {
        tipo: 'Electronica',
        pads: NumberInt(5),
        platos: NumberInt(3),
        modulo: 'TD-02'
      },
      tags: ['bateria', 'electronica', 'roland', 'v-drums'],
      resenas: []
    }
  ]);
  print('[MongoDB Init] Baseline inventory seeded successfully.');
} else {
  print(`[MongoDB Init] Collection already contains ${count} items. Skipping seed.`);
}

print('[MongoDB Init] Database initialization complete.');
