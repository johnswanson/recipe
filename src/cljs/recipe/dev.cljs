(ns recipe.dev
  (:require [recipe.app]
            [devtools.core :as devtools]))

(defonce runonce
  [(devtools/install! [:formatters :hints :async])
   (enable-console-print!)])

(recipe.app/main)
