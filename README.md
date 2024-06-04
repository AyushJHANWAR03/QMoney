*Background*
The Spring framework is used to build the Java backend for QMoney. The QMoney frontend, on the other hand, is written in React - a JavaScript framework. It is not uncommon in web-based applications to have the frontend and backend written in different languages. In order to communicate seamlessly between the frontend and backend, a standard data interchange format is hence required.
QMoney is a visual stock portfolio analyzer. It helps portfolio managers make trade recommendations for their clients.
- Used Tiingo’s REST APIs to fetch stock quotes.
- Computed the annualized returns based on stock purchase date and holding period.
- Refactored code to adapt to an updated interface contract published by the backend team.
- Published the portfolio manager library as a JAR for easy versioning and distribution.
- Created examples to help document library (JAR) usage.
- Added support for a backup stock quote service (Alpha Vantage) to improve service availability.
- Improved application stability with comprehensive error reporting and better exception handling.
- Improved application responsiveness by introducing multithreading.
- Wrote unit tests to measure performance improvements.

How to run :

./gradlew verifyMavenJarsCreated

cd ~/workspace/annual-return-app

chmod +x gradlew

./gradlew build bootrun

cd ~/workspace/annual-return-app

python3 dashboard.py --json trades.json
