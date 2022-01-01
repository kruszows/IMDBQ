# IMDBQ

parses IMDB top 1000 list and creates bktree with embedded hashmap for exact and fuzzy searching of words contained within the individual movie sites

frontend simple jsp form takes in user input query as a string and displays the list of movies whose imdb sites contain the exact or close match to the words within the query string

for the sake of time and keeping this implementation simple, currently strips all punctuation from user input and sites when parsing

to run a compiled version, download https://drive.google.com/file/d/1hXoQu1b2g044zXg5VfLksR9fdAP_iPvI/view?usp=sharing and run using "java -jar IMDBQ.war" from command line on a 
system with java installed and access the site from localhost:8080
