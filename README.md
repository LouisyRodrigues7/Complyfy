
# 📱 Complyfy – Projeto Android (Faculdade)

Este repositório é dedicado ao desenvolvimento do **Complyfy**, um aplicativo Android feito para um projeto da faculdade. Nosso objetivo com este README é manter o projeto organizado e evitar conflitos entre os membros do grupo.

---

## 👥 Organização do Projeto

Todos devem seguir esse padrão:

- Criar sua própria branch com o **seu nome**
- Sempre **puxar a branch `main`** antes de iniciar algo novo
- **NUNCA** fazer push direto na `main`. Sempre usar **Pull Request!**

---

## 🌱 Convenções Importantes

### 🔤 Nomes de Variáveis

| Tipo                | Convenção         | Exemplo           |
|---------------------|-------------------|-------------------|
| Variáveis           | `camelCase`       | `nomeDoUsuario`   |
| Constantes          | `UPPER_SNAKE_CASE`| `MAX_TENTATIVAS`  |
| IDs de Views (XML)  | `prefixo + nome`  | `btnEntrar`, `txtEmail`, `spnCargo` |

---

## 📁 Nomes de Arquivos

- **Arquivos Kotlin (Classes):** `PascalCase`  
  Ex: `LoginActivity.kt`, `UsuarioViewModel.kt`

- **Arquivos de Layout XML:** `snake_case`  
  Ex: `activity_login.xml`, `fragment_home.xml`

---

## 🔒 O que NÃO pode ser alterado sem combinar com o grupo

🚫 Nomes das **Activities**, **Fragments** e **ViewModels** já criados  
🚫 IDs dos componentes de layout (XML) **usados em outras partes**  
🚫 Estrutura de **pacotes e pastas** do projeto  
🚫 Configurações do **Gradle** e **dependências**

---

## 📋 Arquivos Importantes

- `LoginActivity.kt` → Tela inicial com seleção de cargo (Apoio, Coordenador, Gestor)  
- `activity_login.xml` → Layout da tela de login  
- `CoordenadorActivity.kt` → Tela inicial do coordenador  
- `activity_coordenador.xml` → Layout da tela do coordenador  
- `GestorActivity.kt` → Tela inicial do gestor  
- `activity_gestor.xml` → Layout da tela do gestor  
- `ApoioActivity.kt` → Tela inicial do apoio  
- `activity_apoio.xml` → Layout da tela do apoio  
- `strings.xml` → Central de textos estáticos para padronização

---

## ✅ Boas Práticas

- Comentem o código quando necessário
- Mensagens de commit claras, como:  
  `adicionar botão de login`  
  `corrigir bug no dropdown de cargo`
- Use `TODO:` em partes que ainda precisam ser feitas
- Atualize este README se algo importante mudar
=======
# 📱 Complyfy – Projeto Android (Faculdade)

Este repositório é dedicado ao desenvolvimento do **Complyfy**, um aplicativo Android feito para um projeto da faculdade. Nosso objetivo com este README é manter o projeto organizado e evitar conflitos entre os membros do grupo.

---

## 👥 Organização do Projeto

Todos devem seguir esse padrão:

- Criar sua própria branch com o **seu nome**
- Sempre **puxar a branch `main`** antes de iniciar algo novo
- **NUNCA** fazer push direto na `main`. Sempre usar **Pull Request!**

---

## 🌱 Convenções Importantes

### 🔤 Nomes de Variáveis

| Tipo                | Convenção         | Exemplo           |
|---------------------|-------------------|-------------------|
| Variáveis           | `camelCase`       | `nomeDoUsuario`   |
| Constantes          | `UPPER_SNAKE_CASE`| `MAX_TENTATIVAS`  |
| IDs de Views (XML)  | `prefixo + nome`  | `btnEntrar`, `txtEmail`, `spnCargo` |

---

## 📁 Nomes de Arquivos

- **Arquivos Kotlin (Classes):** `PascalCase`  
  Ex: `LoginActivity.kt`, `UsuarioViewModel.kt`

- **Arquivos de Layout XML:** `snake_case`  
  Ex: `activity_login.xml`, `fragment_home.xml`

---

## 🔒 O que NÃO pode ser alterado sem combinar com o grupo

🚫 Nomes das **Activities**, **Fragments** e **ViewModels** já criados  
🚫 IDs dos componentes de layout (XML) **usados em outras partes**  
🚫 Estrutura de **pacotes e pastas** do projeto  
🚫 Configurações do **Gradle** e **dependências**

---

## 📋 Arquivos Importantes

- `LoginActivity.kt` → Tela inicial com seleção de cargo (Apoio, Coordenador, Gestor)  
- `activity_login.xml` → Layout da tela de login  
- `CoordenadorActivity.kt` → Tela inicial do coordenador  
- `activity_coordenador.xml` → Layout da tela do coordenador  
- `GestorActivity.kt` → Tela inicial do gestor  
- `activity_gestor.xml` → Layout da tela do gestor  
- `ApoioActivity.kt` → Tela inicial do apoio  
- `activity_apoio.xml` → Layout da tela do apoio  
- `strings.xml` → Central de textos estáticos para padronização

---

## ✅ Boas Práticas

- Comentem o código quando necessário
- Mensagens de commit claras, como:  
  `adicionar botão de login`  
  `corrigir bug no dropdown de cargo`
- Use `TODO:` em partes que ainda precisam ser feitas
- Atualize este README se algo importante mudar