# lein-sablecc

Lein-SableCC is a leiningen plugin that automates compiling of SableCC grammars.

## Usage

Add the following to your project.clj replacing the sablecc-source-paths with
the directory to search for sablecc sources with the file extension ".scc". 
SableCC is not provided by this plugin but it is required and must be included
as a dependency of your project. The dependency should normally be provided 
in the :dev profile since it is only needed during compilation of the 
grammar sources.  This plugin has been tested against sablecc version 2.x and
3.x, but isn't guaranteed to work with future releases of sablecc.

:plugins [[lein-sablecc "0.1.0-SNAPSHOT"]]<br />
:sablecc-source-paths ["src/sablecc"]<br />
:hooks [leiningen.sablecc.compile]<br />
:profiles {:dev {:dependencies [[sablecc/sablecc "2.18.2"]]}}<br />

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
