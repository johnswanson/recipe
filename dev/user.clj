(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.repl :refer :all]
            [reloaded.repl :refer [system init start stop go reset]]
            [com.stuartsierra.component :as component]
            [clojure.string :as str]
            [cheshire.core :as json]
            [taoensso.timbre :as log]
            [taoensso.sente :as sente]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pp pprint]]
            [clojure.test :refer [run-all-tests run-tests]]
            [clojure.java.shell :refer [sh]]
            [figwheel-sidecar.repl :as r]
            [figwheel-sidecar.system :as fs]
            [figwheel-sidecar.repl-api :as ra]
            [system.components
             [watcher :refer [new-watcher]]]
            [recipe.systems]
            [recipe.config :refer [dev-config]]))

(clojure.tools.namespace.repl/set-refresh-dirs "dev" "src/clj")

(log/set-level! :debug)
(reset! sente/debug-mode?_ false)

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defrecord Figwheel [config]
  component/Lifecycle
  (start [component]
    (assoc component :figwheel (ra/start-figwheel! config)))
  (stop [component]
    (dissoc component :figwheel)))

(defn figwheel [config]
  (->Figwheel config))

(defn scss-compiler
  []
  (new-watcher ["./resources/scss"]
               (fn [action f]
                 (log/infof "%s %s, rebuilding app.css" action f)
                 (sh "sassc"
                     "-m"
                     "-I" "resources/scss/"
                     "-t" "nested"
                     "resources/scss/app.scss"
                     "resources/public/css/app.css")
                 (log/info "app.css build complete"))))

;; We keep this separate from the overall system in order to start it with CIDER.
(def figwheel-component
  (figwheel (:figwheel dev-config)))

(defn cljs-repl-start []
  (component/start figwheel-component)
  (ra/cljs-repl))

(defn dev-system []
  (log/infof "Starting with config:\n%s" (with-out-str (pprint dev-config)))
  (let [sys (-> (recipe.systems/base-system dev-config)
                (assoc :scss-compiler (scss-compiler)))]
    (apply component/system-map (flatten (into [] sys)))))

(reloaded.repl/set-init! dev-system)

