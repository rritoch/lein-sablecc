# Lein-SableCC

Lein-SableCC is a leiningen plugin that automates compiling of SableCC grammars.

## Usage

To enable this plugin it must be added to your leiningen project file in the 
**:plugins** list. This plugin depends on, but does not provide, SableCC. In most
cases SableCC should be provided as a dependency in the **:dev** profile of your
project. This plugin has been tested against **SableCC 2.x** and **3.x**.
<br />
<br />
**Example:**

```
:plugins [[lein-sablecc "1.1.0"]]
:profiles {:dev {:dependencies [[sablecc/sablecc "2.18.2"]]}}
```

This plugin only recognizes grammar files which have the file extension **.scc** 
and will only search for them in the directories listed in **:sablecc-source-paths**
in your leiningen project file.
<br />
<br />
**Example:**

```
:sablecc-source-paths ["src/sablecc"]
```

To generate the java sources from the grammar files and compile them run the 
following command from a shell.

```
lein sablecc compile
```

All of the sources are generated in the **:target-path** sub-directory 
**generated-sources/sablecc** and will be compiled to the **:compile-path**.<br />

**Implementation note:** Java sources are only regenerated if the generated 
**Parser.java** file is missing or has a modification time that is less than the 
modification time of the **.scc** grammar source file.  This optimization 
ensures that sources are only recompiled when the grammar file has been 
modified.  The **Parser.java** file is located using the **Package** declaration 
in the **.scc** grammar source file. If this plugin cannot locate a **Package** 
declaration in the **.scc** grammar file, or if it has been commented out, 
than the **.scc** grammar source file will not be compiled.

## Automation hook

For convenience a hook is provided which will automatically compile the **.scc**
grammar files each time the **javac** leiningen task is run, such as when running 
'**lein compile**' from a shell. To enable this hook you will need to add it to
your leiningen project hooks.

**Example:**

```
:hooks [leiningen.sablecc.compile]
```

When this hook is enabled '**lein javac**' can be run from the command line to
compile your grammar source files.

## Change Log

### 1.0.0

* Release - January 2, 2015

### 1.1.0

* New Feature - Added Source Generation pre-cleaner
* Bugfix - Repaired string escape sequence handling
* Bugfix - Added generated .dat files to target classpath
* Release - January 5, 2015

## License

Copyright © 2014-2015 Ralph Ritoch <rritoch@gmail.com>

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
