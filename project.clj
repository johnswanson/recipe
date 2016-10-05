(defproject recipe "0.1.0-SNAPSHOT"
  :description "fixme"
  :url "https://github.com/johnswanson/recipe"
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :plugins [[lein-environ "1.1.0"]]
  :profiles {:dev [:secrets {:source-paths ["dev"]
                             :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                            [binaryage/devtools "0.8.2"]
                                            [figwheel-sidecar "0.5.8" :scope "test"]
                                            [devcards "0.2.1-7"]
                                            [com.cemerick/piggieback "0.2.1"]
                                            [reloaded.repl "0.2.3"]]}]}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :gpg}}
  :dependencies [[org.clojure/clojure "1.9.0-alpha12"]
                 [org.clojure/clojurescript "1.9.229"]
                 [org.clojure/core.async "0.2.391"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [com.datomic/datomic-pro "0.9.5394"]
                 [im.chit/hara.io.watch "2.4.4"]
                 [org.danielsz/system "0.3.2-SNAPSHOT"]
                 [ring-middleware-format "0.7.0"]
                 [compojure "1.5.1"]
                 [re-frame "0.8.0"]
                 [day8.re-frame/http-fx "0.0.4"]
                 [day8.re-frame/async-flow-fx "0.0.6"]
                 [environ "1.1.0"]
                 [oauth-clj "0.1.15"]
                 [http-kit "2.2.0"]
                 [org.apache.httpcomponents/httpclient "4.5.2"]
                 [enlive "1.1.6"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-anti-forgery "1.0.1"]
                 [cheshire "5.6.3"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.taoensso/sente "1.10.0" :exclusions [com.taoensso/encore]]
                 [com.taoensso/timbre "4.7.4"]
                 [datomic-schema "1.3.0"]
                 [hiccup "1.0.5"]
                 [kibu/pushy "0.3.6"]
                 [markdown-clj "0.9.89"]])

