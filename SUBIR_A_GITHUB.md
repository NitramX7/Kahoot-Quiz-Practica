# 🚀 Comandos para Subir a GitHub (SIMPLE)

## ⚠️ La autenticación de `gh` no funcionó

No te preocupes, vamos a hacerlo de forma manual (más fácil):

## Paso 1: Crear el repositorio en GitHub.com

1. **Ve a**: https://github.com/new
2. **Nombre del repositorio**: `quiz-live-practica`
3. **Descripción**: `Quiz Live - Sistema tipo Quizizz con Spring Boot y PSP Concurrency`
4. **Visibilidad**: ✅ **Public**
5. ❌ **NO marques** "Add a README file"
6. ❌ **NO marques** "Add .gitignore"
7. Click en **"Create repository"**

## Paso 2: Conectar y Subir (Ejecuta estos comandos)

Copia y pega estos comandos en tu terminal (uno por uno):

```bash
# 1. Añadir el remoto (usa TU usuario de GitHub)
git remote add origin https://github.com/NitramX7/quiz-live-practica.git

# 2. Renombrar la rama a main
git branch -M main

# 3. Subir todo el código
git push -u origin main
```

## Paso 3: Verificar

Cuando terminen los comandos:
1. Abre: https://github.com/NitramX7/quiz-live-practica
2. Deberías ver todo el código subido ✅

---

## 📦 ¿Qué se va a subir?

- ✅ README.md (documentación completa)
- ✅ CONCURRENCY.md (explicación PSP)
- ✅ Código fuente completo (7 entidades, servicios, controladores)
- ✅ Motor concurrente GameEngineService
- ✅ Configuración Spring Boot
- ✅ Datos de prueba

---

## ❓ Si da error "remote already exists"

```bash
# Elimina el remoto anterior
git remote remove origin

# Vuelve a añadirlo
git remote add origin https://github.com/NitramX7/quiz-live-practica.git

# Sube el código
git push -u origin main
```

---

**¡Avísame cuando esté subido!** 🎉
