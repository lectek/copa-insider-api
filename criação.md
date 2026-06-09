🚀 Comandos Maven essenciais + como usar
🔹 1. mvn clean
Limpa o diretório target/, que contém os arquivos compilados da última build.

Exemplo de uso:
mvn clean
Para que serve?

Remove resíduos de builds anteriores.

Evita conflitos causados por versões antigas de .class, .jar, logs ou testes antigos.

🔹 2. mvn compile
Compila apenas o código-fonte principal (o que está em src/main/java), sem executar testes ou gerar JAR.

Exemplo:
mvn compile
Quando usar?

Para verificar se a estrutura e os imports do seu código estão corretos.

Útil em fases iniciais de correção.

🔹 3. mvn test
Executa somente os testes automatizados (em src/test/java).

Exemplo:
mvn test
Dica:

Você pode ver onde os testes estão falhando.

Use após cada alteração em classes de serviço ou controller com cobertura de testes.

🔹 4. mvn clean test
Combina a limpeza com a execução dos testes.

Exemplo:
mvn clean test
🔹 5. mvn package
Compila o código e empacota em um .jar (ou .war).

Exemplo:
mvn package
Para Spring Boot:

O .jar gerado será executável.

Fica salvo em: target/redemaisfarma-api-<versão>.jar

🔹 6. mvn install
Mesma coisa do package, mas também instala o .jar no repositório local (.m2/repository).

Exemplo:
mvn install
Quando usar?

Quando você tem múltiplos projetos que dependem uns dos outros.

Para usar a API como dependência em outro projeto Java.

🔹 7. mvn clean install -DskipTests
Compila, instala e ignora os testes.

Exemplo:
mvn clean install -DskipTests
Para que serve?

Para forçar a geração do .jar quando os testes estão quebrados.

Muito usado em builds temporários de desenvolvimento.

🔹 8. mvn dependency:tree
Mostra a árvore de dependências do projeto.

Exemplo:
mvn dependency:tree
Por que é útil?

Detecta conflitos de versões.

Ajuda a entender quais libs estão puxando outras libs.

🔹 9. mvn help:effective-pom
Gera o POM final com todas as heranças e perfis aplicados.

Exemplo:
mvn help:effective-pom
Pra quê usar?

Para saber exatamente quais versões e configurações estão ativas no seu projeto.

🔹 10. mvn help:effective-settings
Exibe as configurações reais do Maven (settings.xml) que estão ativas.

Exemplo:
mvn help:effective-settings
🔹 11. mvn validate
Verifica se o projeto está estruturado corretamente (sem compilar).

🔹 12. mvn spring-boot:run
Roda sua aplicação direto pelo Maven, sem precisar digitar java -jar.

Exemplo:
mvn spring-boot:run
Obs: Funciona apenas se o mainClass estiver configurado no pom.xml.

🔹 13. mvn versions:display-dependency-updates
Mostra as dependências que têm versões mais novas disponíveis.

Exemplo:

mvn versions:display-dependency-updates
Ótimo para manter o projeto atualizado.

🔹 14. mvn versions:use-latest-versions
Atualiza automaticamente o pom.xml para usar as versões mais recentes de cada dependência.

Use com cuidado! Pode quebrar seu projeto se houver incompatibilidades.

🧪 Extras para rodar com perfil de testes H2
mvn clean test -Dspring.profiles.active=test

Ou para rodar a aplicação com o H2 ativado via perfil:
mvn spring-boot:run -Dspring-boot.run.profiles=test


🔥 Comandos úteis do Java (fora do Maven)
✅ Rodar o .jar gerado:

java -jar target/redemaisfarma-api-0.0.1-SNAPSHOT.jar
