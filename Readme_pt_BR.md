# The Hand

## Requisitos
- Postgresql [download](https://www.postgresql.org/download/)
- Java SDK 8 ou Superior [download](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
- Sbt [download](https://www.scala-sbt.org/download.html)

### Como configurar o banco 
Criar um usuário e database
link para exemplo

### Configurar a aplicação
Encontra-se um exemplo no /src/main/resources/application.conf

#### Configuração de teste que roda em memória
```
testConfig = {
  connectionPool      = disabled
  url                 = "jdbc:h2:mem:testdb"
  driver              = "org.h2.Driver"
  keepAliveConnection = true
}
```

#### Configuração do banco
```
dbconfig = {
  connectionPool = disabled
  url = "jdbc:postgresql:DATABASENAME?user=USERDBNAME&password=PASSDB"
  profile = ""
  driver = "org.postgresql.Driver"
  keepAliveConnection = true
  users = "USERDBNAME"
  password = "PASSDB"
  maxActive = 2
  maxConnections = 20
  numThreads = 10
}
```

#### Configuração de projeto
Cada projeto, possui uma configuração distinta com os dados de conexão ao repositório e conexão ao banco de dados.

É importante alternar para cada projeto ```database_sufix = "demo_"```, para manter separado cada projeto com o uso de sufixos. 

```
projectDemo = {
  user = "YOUR USER"
  pass = "YOUR PASS"
  url = "YOUR SVN URL"
  database_sufix = "demo_"
  mode = "auto"
  start_revision = 1  //use only if mode is no auto
  end_revision = 1000 //use only if mode is no auto
  task_model = {
    patternParser = "(#\\d)\\d+" //task or fix #NUMBER
    patternSplit = "#" //task or fix #NUMBER
    separator = ""
  }
}
```

Define qual a forma que é extraída a informação sobre as tarefas
```
patternParser = "(#\\d)\\d+" //task or fix #NUMBER
patternSplit = "#" //task or fix #NUMBER
separator = ""
```

##### Modo de scan scm
Há dois modos de operação para scan sobre o scm

###### Modo Automático
Pesquisa nos registros qual a última versão já salva, e no scm qual a última versão enviada. Utilizando esses dados para carregar os registros.
```
mode = "auto"
```

###### Modo Manual
Utilizar o número de revisão definido pelo usuário para definir o carregamento dos registros.
Atualmente não atualiza registros já existentes. Não possuindo um comportamento de “insertOrUpdate”.
```
mode = "off"
start_revision = 1 
end_revision = 1000
```

##### Configuração do target
Para agregação dos dados de fluxo ágil utilizando o [TargetProcess](targetprocess.com)
Atualmente o thehand não possui suporte para outros meios de autenticação.
```
target = {
  user = "YOUR USER"
  pass = "YOUR PASS"
  url = "YOUR URL"
}
```

### Rodar
```
> sbt run -Dconfig.file=/... path .../application.conf
```

#### Testar
```
> sbt test
```
