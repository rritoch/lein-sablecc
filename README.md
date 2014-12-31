# lein-sablecc

Lein-SableCC is a leiningen plugin that automates compiling of SableCC grammars.

## Usage

Add the following to your project.clj replacing the sablecc-source-paths with
the directory to search for sablecc sources with the file extension .scc

:plugins [[lein-sablecc "0.1.0-SNAPSHOT"]]<br />
:sablecc-source-paths ["src/sablecc"]<br />
:hooks [leiningen.sablecc.compile]<br />

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
