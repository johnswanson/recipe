(defproject recipe "0.1.0-SNAPSHOT"
  :description "fixme"
  :url "https://github.com/johnswanson/recipe"
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :plugins [[lein-environ "1.1.0"]]
  :profiles {:dev [:secrets {:source-paths ["dev"]
                             :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                            [binaryage/devtools "0.8.2"]
                                            [figwheel-sidecar "0.5.0-SNAPSHOT" :scope "test"]
                                            [devcards "0.2.1-7"]
                                            [reloaded.repl "0.2.1"]]}]}
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :gpg}}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [com.datomic/datomic-pro "0.9.5394"]
                 [im.chit/hara.io.watch "2.1.7"]
                 [org.danielsz/system "0.3.1"]
                 [ring-middleware-format "0.7.0"]
                 [compojure "1.5.0"]
                 [re-frame "0.8.0"]
                 [day8.re-frame/http-fx "0.0.4"]
                 [day8.re-frame/async-flow-fx "0.0.6"]
                 [environ "1.1.0"]
                 [oauth-clj "0.1.15"]
                 [http-kit "2.1.18"]
                 [org.apache.httpcomponents/httpclient "4.5.2"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-anti-forgery "1.0.1"]
                 [cheshire "5.6.1"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.taoensso/sente "1.9.0-beta3"]
                 [com.taoensso/timbre "4.3.1"]
                 [datomic-schema "1.3.0"]
                 [hiccup "1.0.5"]
                 [kibu/pushy "0.3.6"]
                 [markdown-clj "0.9.89"]])

