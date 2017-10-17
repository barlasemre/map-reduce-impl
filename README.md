# Word Counter

Word Counter Application with simple map-reduce aproach

This project is a simple word counter application which uses map reduce algorithm. Application writes the words counted to the Mongo db. In order to run the application, MongoDB should be run before the project. Input file directory should only contain files to process. So, first clean the files under "input" directory, then put all input files into this directory before run the application.

Running MongoDB:

Windows:
- Run C:\Program Files\MongoDB\Server\3.4\bin\mongod.exe
- After running mongo.exe, run C:\Program Files\MongoDB\Server\3.4\bin\mongo.exe

# What is Map Reduce Algorithm?

It is a well known approach to analyse big data. The first is the map job, which takes a set of data and converts it into another set of data, where individual elements are broken down into tuples (key/value pairs). The reduce job takes the output from a map as input and combines those data tuples into a smaller set of tuples. As the sequence of the name MapReduce implies, the reduce job is always performed after the map job.

# Mapping Procedure
In order to analyse big text file, we should devide problem into smaller pieces. Map job simply reads and divides word counts in a big text file and seperate it for reduce job. During the mapping process, we will have smaller pieces of big text data under the reduce directory, as you can see from pictures:

![Screenshot](/Screenshots/map-reduce-1.JPG) ![Screenshot](/Screenshots/map-reduce-2.JPG)

# Reduce Procedure

As we mentioned in the description, reduce procedure takes pieces from reduced directory and combines them for the final solution. After reduce procedure, there will be only 1 file which containes the final result of application.

# Writing Mongo DB

After reduce job, application creates an object which contains whole map data. Rather writing all map elements into Mongo, I prefered to write just one object which contains whole word counts. This will have less cost and more performance. In order to see most and least appeared words, we get data from map variable of Words object.
