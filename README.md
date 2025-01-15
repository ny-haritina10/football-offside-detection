1. Install Open CV 4.7.0
    - use .exe file
    - after installation: 
        -> find: 
            - " ... PATH WHERE YOU EXTRACTED THE .exe FILE ..."\opencv\build\x64\vc16\bin
            - " ... PATH WHERE YOU EXTRACTED THE .exe FILE ..."\opencv\build\java\x64
        -> add those path to `environnement variable` under the variable `path`

2. Install Maven 
    -> to check if maven is installed : 
        - mvn --version

3. compile and run the project on vs code: 
    - use `CMD` instead of powershell (vs code terminal is not working)
    - cd `football-offside-detection` 
    - mvn compile
    - mvn exec:java -Dexec.mainClass="mg.itu.App"