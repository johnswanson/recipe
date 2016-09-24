(ns cards.core
  (:require [goog.events :as events]
            [reagent.core :as reagent]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [devtools.core :as devtools])
  (:import [goog History]
           [goog.history EventType]))

