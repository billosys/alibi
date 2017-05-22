### Setup and Config

#### Initialization

Clone the repo. Then, inside the repo, initialize a time tracker:

```
$ make var/data/timi.db
```

This will create a sqlite3 db with filename `var/data/timi.db`, which contains
the right schema, but has no data yet.


#### Initial Configuration

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


#### Authenticating with Github

Tímı comes with support for OAuth2, and in particular, the ability for users to
authenitcate in the app using Github. All that is required is for adminstrators
to change their configuration.

For example, when an adminstrator copies the sample config to `config/local`,
it looks like this:

```clj
{:cookie-encryption-key "abcdef0123456789"
 :selmer-caching? false
 :persistence :sqlite
 :persistence-strategies {}
 :sqlite {:subprotocol "sqlite"
          :subname "var/data/timi.db"}
 :authentication :single-user
 :single-user {:username "Alice"}
 :log {:level :debug
       :ns [timi
            ring
            compojure
            leiningen
            org.httpkit
            org.clojure]
       :http-requests? true
       :http-skip "/dist.*|/assets.*"}
 :httpd {:port 5099
         :host "localhost"}}
```

To authenticate with Github, take the following steps:

1. Replace `:authentication :single-user` with `:authentication :github`
2. Remove `:single-user {:username "Alice"}`
3. Set up an [OAuth2 app on Github](https://github.com/settings/applications)
   that points to your deployment host with the "/authorize" Tímı endpoint
   (for testing, it's perfectly valid to enter
   "http://localhost:5099/authorize" in the Github form).
4. Copy the "Client ID" and "Client Secret" from the Github page in the
   previous step to a new `:github` entry in your config, e.g.:

   ```
    :authentication :github
    :github {
      :client-id "abcdef0123456789"
      :client-secret "abcdef0123456789abcdef0123456789"}
   ```

Restart your app, and users can now log into Tímı with Github :-)
