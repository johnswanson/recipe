(ns recipe.dev
  (:require [recipe.app]
            [devtools.core :as devtools]))

(defonce runonce
  [(devtools/install!)
   (enable-console-print!)])

(recipe.app/main)
