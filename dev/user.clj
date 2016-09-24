(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.repl :refer :all]
            [reloaded.repl :refer [system init start stop go reset]]
            [com.stuartsierra.component :as component]
            [datomic.api :as d]
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
            [recipe.systems]))

(log/set-level! :debug)
(reset! sente/debug-mode?_ false)

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def figwheel-config
  {:figwheel-options {:css-dirs ["resources/public/css"]}
   :build-ids        ["dev" "cards"]
   :all-builds
   [{:id           "cards"
     :figwheel     {:devcards true}
     :source-paths ["src/cljs"]
     :compiler     {:main                 'cards.core
                    :asset-path           "/js/compiled_cards"
                    :output-to            "resources/public/js/compiled_cards/app.js"
                    :output-dir           "resources/public/js/compiled_cards"
                    :source-map           true}}
    {:id           "dev"
     :figwheel     true
     :source-paths ["src/cljs"]
     :compiler     {:main                 'recipe.dev
                    :asset-path           "/js/compiled"
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled"
                    :optimizations        :none
                    :source-map           true}}]})


(defrecord Figwheel [config]
  component/Lifecycle
  (start [component]
    (assoc component :figwheel (ra/start-figwheel! config)))
  (stop [component]
    (dissoc component :figwheel)))

(def figwheel (->Figwheel figwheel-config))

(def scss-compiler
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

(defn dev-system []
  (let [sys (-> recipe.systems/dev-system
                (assoc :figwheel figwheel)
                (assoc :scss-compiler scss-compiler))]
    (apply component/system-map (flatten (into [] sys)))))

(reloaded.repl/set-init! dev-system)
