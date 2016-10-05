(ns cards.core
  (:require [goog.events :as events]
            [reagent.core :as reagent]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [devtools.core :as devtools]
            [cljs.test :refer [testing is]]
            [cards.events])
  (:import [goog History]
           [goog.history EventType]))

(defonce runonce
  [(devtools/install! [:formatters :hints :async])
   (enable-console-print!)])
