# UDOO Android Serial

![alt tag](http://www.udoo.org/wp-content/uploads/2014/12/logoogo.png)

Library for control Arduino on Udoo boards

# Usage

  1. Include the library as local library project or add the dependency in your build.gradle.
        
       repositories {
           maven {
               url  "http://dl.bintray.com/udooboard/maven"
           }
       }

        ...

        dependencies {
            compile 'org.udoo:udooandroidserial:0.1'
        }
      
