# Como ejecutar QMS (estado actual)

## 1. Requisitos
- Java 8 o superior.
- MySQL en ejecucion (XAMPP sirve).
- Base creada/importada con `db/clinica_qms.sql`.

## 2. Variables de entorno (modo MySQL)
En PowerShell:

```powershell
$env:QMS_REPO="mysql"
$env:QMS_DB_URL="jdbc:mysql://localhost:3306/clinica_qms"
$env:QMS_DB_USER="root"
$env:QMS_DB_PASSWORD=""
```

## 3. Opcion A: ejecutar con Maven
Desde la carpeta `proyecto`:

```powershell
mvn clean compile
mvn exec:java
```

## 4. Opcion B: ejecutar sin Maven (validada)
### 4.1 Compilar
Desde la carpeta `proyecto`:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac --release 8 -d out (Get-ChildItem -Recurse -Filter *.java src\main\java | ForEach-Object { $_.FullName })
```

### 4.2 Buscar el driver de MySQL

```powershell
$jar = Get-ChildItem "$env:USERPROFILE\.m2\repository\com\mysql\mysql-connector-j" -Recurse -Filter "mysql-connector-j-*.jar" | Sort-Object FullName -Descending | Select-Object -First 1 -ExpandProperty FullName
```

### 4.3 Ejecutar
Si estas en la carpeta `proyecto`:

```powershell
java -cp "out;$jar" com.clinica.qms.App
```

Si estas en la carpeta padre `ProyectoSiglo21`:

```powershell
java -cp "proyecto\out;$jar" com.clinica.qms.App
```

## 5. Usuarios de prueba (modo MySQL)
La app lee operadores de la tabla `operador`.
Si la tabla esta vacia, crea operadores semilla automaticamente:

- `lperez / admin123`
- `tacosta / lab123`
- `nruiz / inter123`

## 6. Flujo rapido de prueba
1. Opcion `4` en menu principal: ABM de areas y boxes.
2. Opcion `1`: crear turno en autogestion.
3. Opcion `2`: login operador y llamar siguiente.
4. Opcion `3`: ver pantalla de sala de espera.

## 7. Problemas comunes
- Error `No suitable driver found`: falta agregar `mysql-connector-j` al classpath en ejecucion sin Maven.
- Error de conexion: revisar que MySQL este levantado y que `QMS_DB_URL`, `QMS_DB_USER`, `QMS_DB_PASSWORD` sean correctos.
- Si `mvn` no existe: ejecutar con la Opcion B (sin Maven).
