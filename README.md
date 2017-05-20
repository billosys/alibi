# Tímı

*A time tracker written in Clojure(Script)*

#### Contents

* [About](#about-)
* [Screenshot](#screenshot-)
* [Features](#features-)
* [Dependencies](#dependencies-)
* [Documentation](#documentation-)
* [Usage](#usage-)
  * [Initialization](#initialization-)
  * [Configuration](#configuration-)
  * [Build](#build-)
  * [Projects and Tasks Population](#projects-and-tasks-population-)
  * [Startup](#startup-)
* [History](#history-)
* [License](#license-)

## About [&#x219F;](#contents)

Tímı is a project time-tracking tool intended for use by uindividuals, non-profits, and small companies.


## Screenshot [&#x219F;](#contents)

Tímı in action:

![Tími screenshot](docs/screenshot-2.png)


## Features [&#x219F;](#contents)

This is far from a fully fledged time tracker, yet it has the following
features, and more is on the way!

* Categorize time entries by project and task.
* Graphical overview of your weekly activity.
* Indicate whether entries are billable or not.
* Tasks have an associated billing method, either fixed price, overhead or hourly.
* Create projects and tasks via a CLI, and the REPL.
* Extension points for a different data source implementations.
* OpenID Connect and single user authentication.
* Out of the box Sqlite3 database support.


## Dependencies [&#x219F;](#contents)

The prerequisites for this route are:

- Have a Git client installed.
- Make sure Java is installed.
- Make sure [lein](https://leiningen.org/) is installed.
- Have `npm` installed
- Ensure that `make` is installed


## Documentation [&#x219F;](#contents)

Forthcoming ...


## Usage [&#x219F;](#contents)

### Initialization [&#x219F;](#contents)

Clone the repo. Then, inside the repo, initialize a time tracker:

```
$ make var/data/timi.db
```

This will create a sqlite3 db with filename `var/data/timi.db`, which contains
the right schema, but has no data yet.


### Configuration [&#x219F;](#contents)

Before we create some data, you first need to setup a config file. For local
development, copy the sample config:

```
$ cp config/sample/config.edn config/local
```

You'll want to change the encryption cookie, though. Generate a new one with:

```
$ make cookie
```

Then update your new `config/local/config.edn` file, replacing the same value
for `:cookie-encryption-key` with the one `make cookie` generated for you.


### Build [&#x219F;](#contents)

The Clojure and ClojureScript for the project can be built in one go with the
following:

```
$ make
```

If you just want to do one or the other, you may use `make build-clj` or
`make build-cljs`.


### Projects and Tasks Population [&#x219F;](#contents)

Now we can populate the database with some projects and tasks. Here's how you
create a project:

```
$ lein timi-create-project "Tími" :billing-method :hourly
```
```clj
Creating new project:
{:project-name "Tími", :billing-method :hourly}
Done. Id for project is 1
```

The project ID that is returned is what you will use when creating tasks for
that project. For example:

```
$ lein timi-create-task "Project management" :for-project 1 :billing-method :overhead
$ lein timi-create-task "Programming" :for-project 1
```


### Startup [&#x219F;](#contents)

For deployment and non-interactive development, simply run the server:

```
$ make run
```

If everything went well, there should now be a web server running on
[http://localhost:5099](http://localhost:5099) (or whatever port you updated your 
local configuration to have). Navigate to it and have fun!


## History [&#x219F;](#contents)

This project started as a fork of the
[Infi Alibi Time Tracker](https://github.com/infi-nl/alibi), but has since
undergone major refactorings (with more to come). What started as a
handful of changes for our own needs and aesthetics has evolved into something
that is a new application in its own right. Still, Tímı owes much of its DNA
and all of its beginnings to Unfi's Alibi.


## License [&#x219F;](#contents)

Copyright © 2017 Infi Holding B.V. and contributers.

Copyright © 2017 Billo Systems, Ltd. Co.

Distributed under the Mozilla Public License Version 2.0
