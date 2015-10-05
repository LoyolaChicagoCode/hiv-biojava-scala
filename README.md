# Overview

Beginnings of the HIV evolution parser that splits Genbank data into mutliple
FASTA files.

# Prerequisites

## Required

- Java Development Kit (JDK) through your package management system or from [Oracle](http://www.oracle.com/technetwork/java/javase/downloads)
- [sbt](http://www.scala-sbt.org/)

These really are the only required prerequisites.

## Optional

- [Eclipse 4.2.x IDE for Java Developers](http://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/junosr1)
- [Scala IDE Eclipse plugin](http://scala-ide.org/download/milestone.html#scala_ide_21_milestone_3) corresponding to your Eclipse version

# Command-line

## Feteching the data

The Genbank files are no longer being kept on GitHub. They're now in a 
figshare dataset at http://figshare.com/articles/hivdb/1564844. You don't need
to go there to download the files. A script is provided to fetch the data 
sets into data/ (and this folder is on the .gitignore list).

To populate the data, do this from the checkout directory:

    $ ./scripts/get-data.sh 

## Running the sample application

This creates the launch script (so it is easy to run like a regular command line program):

    $ sbt stage

Then run the application outside of sbt like this:

    $ target/start filename+ (list of files)

As an example...

    $ target/start data/JX447638.gb data/JX447639.gb

# Generating FASTA by gene

You need to have a folder (that doesn't exist a priori) to write the results. If the folder exists
from a previous run, you must rename or delete it.

    $ target/start data/*.gb | python scripts/postprocess.py <folder-name> 

I usually do:

    $ rm -rf results
    $ target/start data/*.gb | python scripts/postprocess.py results 
    


# Eclipse

## Generating the configuration files

    $ sbt eclipse

# Experimental Mongo+Python Postprocessor

Make sure to install all needed dependencies using Python pip:

    $ sudo pip install flask pymongo gunicorn

MongoDB is a promising NoSQL database that can really put this work on steroids.

This is sort of a mockup, which I think should be added to the Scala version!

- postprocess-mongo.py: Writes the delimited data as individual documents to the Mongo database
- postprocess-fasta.py: Extracts the records for one of the genes and generates a FASTA file (to standard output)

Example usage to get the "gag" gene:

    $ target/start data/*.gb | python scripts/postprocess-mongo.py collection-name

where *collection-name* should be replaced with a new (and empty) collection name.

    $ python scripts/postprocess-fasta.py collection-name gene-name

where *collection-name* should be replaced with a new (and empty) collection name and gene-name should be one of the extracted genes (e.g. gag, env, etc.)

If you want to write the FASTA output to a file, do:

    $ python scripts/postprocess-fasta.py collection-name gene-name > gene-name.fasta

This is much more flexible than our previous effort, which basically writes the files to hard-coded filenames. By using this process, we'll eventually be able to add or change the original GenBank data but have a completely decoupled process for generating FASTA for further analysis and visualization.

# Running Web Service (Development)

Run gunicorn in daemon mode with 4 (-w) worker threads:

    $ cd scripts
    $ gunicorn -w 4 -b 0.0.0.0:5000 webservice:app -D

# Running Web Service (Deployment)

Install supervisor daemon:

    $ apt-get install supervisor


Create /etc/supervisor/conf.d/hivservice.conf with these contents::

    [program:hivservice]
    command = gunicorn -w 8 -b 0.0.0.0:5000 hivservice:app
    directory = /home/xyz/Work/hiv-biojava-scala/scripts/
    user = xyz

This assumes you have checked out hiv-biojava-scala to ~xyz/Work.

Then reread the configuration and restart:

    $ sudo supervisorctl reread
    $ sudo supervisorctl start hivservice

And if you want to stop it:

    $ sudo supervisorctl stop hivservice

This setup pretty much rocks, because it ensures you have daemonized the 
service properly. Among other things, if a reboot is required of the server,
supervisord will restart your Flask service(s).

I'm working to add notes about nginx for proxying,
but this is a separate concern from getting the service up and running.

# Apache Integration

This is an example of how to set up a v-host entry in Apache:

    # Place any notes or comments you have here
    # It will make any customisation easier to understand in the weeks to come

    # domain: domain1.com
    # public: /var/www/vhosts/introcs.cs.luc.edu/domain.com/

    <virtualhost *:80>
      # Admin email, Server Name (domain name) and any aliases
      ServerAdmin webmaster@hiv.mydomain.com
      ServerName  hiv.mydomain.com
      #ServerAlias www.hiv.mydomain.com


      # Index file and Document Root (where the public files are located)
      DirectoryIndex index.html
      DocumentRoot /var/www/vhosts/hiv.mydomain.com/htdocs


      # Custom log file locations
      LogLevel warn
      ErrorLog  /var/www/vhosts/hiv.mydomain.com/log/error.log
      CustomLog /var/www/vhosts/hiv.mydomain.com/log/access.log combined

      # Proxy Support
      ProxyPass / http://localhost:5000/
      ProxyPassReverse / http://localhost:5000/

    </virtualhost>

Pl

If you are on Ubuntu (where we are), put this into a file in your
sites-available folder, e.g. /etc/apache2/sites-available/hivdb.mydomain.com.

We actually don't need the vhosts directory structure you see here, but on the
servers we manage, we use a standard layout for any virtual host, especially
if we want to serve some static files at some point in the future. We also
like having site-specific logs.

Please note that you need to have a number of Apache modules working to get
virtual hosts and proxying working correctly. We're not covering that here.


