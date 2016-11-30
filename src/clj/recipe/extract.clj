(ns recipe.extract
  (:require [org.httpkit.client :as http]
            [clojure.core.memoize :as memoize]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [cemerick.url :refer [url]]))

(defn html' [url]
  (html/html-resource (java.net.URL. url)))

(def html (memoize/fifo html' :fifo/threshold 5))

(defn abs-url [base path]
  (try
    (str (url path))
    (catch java.net.MalformedURLException e
      (-> base (url) (assoc :path path) (str)))))

(def remove-el (constantly nil))

(defn host [url] (.getHost url))

(defn clean-str [s]
  (-> s
      (str/split #"\n")
      (#(map str/trim %))
      (#(str/join "\n\n" %))
      (str/replace (-> 160 char str) " ")))

(defprotocol IExtractable
  "Something we can extract recipe data from"
  (images [this])
  (ingredients [this])
  (title [this])
  (procedure [this])
  (notes [this]))

(defrecord SmittenKitchenExtractable [url]
  IExtractable
  (images [this]
    (filter
     #(not (str/ends-with? % ".gif"))
     (map
     #(first (html/attr-values % :src))
     (html/select (html url) [:img]))))
  (ingredients [this]
    (map clean-str
         (-> (html url)
             (html/select [:li.jetpack-recipe-ingredient])
             (html/texts))))
  (title [this]
    (-> (html url)
        (html/select [:h1.entry-title])
        (html/texts)
        (first)
        (clean-str)))
  (procedure [this]
    (-> (html url)
        (html/select [:div.jetpack-recipe-directions])
        (html/texts)
        (str/join)
        (clean-str)))
  (notes [this]
    (-> (html url)
        (html/select [:div.jetpack-recipe-notes])
        (html/transform [:script] remove-el)
        (html/texts)
        (str/join)
        (clean-str))))

(defrecord SeriousEatsExtractable [url]
  IExtractable
  (images [this]
    (->> (html/select (html url) [:img])
         (map #(first (html/attr-values % :src)))
         (map #(abs-url url %))))
  (ingredients [this]
    (map clean-str
         (-> (html url)
             (html/select [:li.ingredient])
             (html/texts))))
  (title [this]
    (-> (html url)
        (html/select [:h1.recipe-title])
        (html/texts)
        (first)
        (str/lower-case)
        (clean-str)))
  (procedure [this]
    (-> (html url)
        (html/select [:div.recipe-procedure-text])
        (html/texts)
        (#(map str/trim %))
        (#(str/join "\n\n" %))))
  (notes [this]
    (-> (html url)
        (html/select [:div.recipe-introduction-body :> #{:p :ul}])
        (html/texts)
        (#(map str/trim %))
        (#(str/join "\n\n" %)))))

(def serious-eats? (partial re-find #"seriouseats\.com"))
(def smitten-kitchen?  (partial re-find #"smittenkitchen\.com"))

(defn site [url]
  (cond
    (serious-eats? url) :serious-eats
    (smitten-kitchen? url) :smitten-kitchen))


(def blacklist #{:script html/comment-node})
(def whitelist
  #{:a :b :blockquote :body :code :del :dd :dl :dt
    :em :h1 :h2 :h3 :i :kbd :li :ol :p :pre
    :s :sup :sub :strong :strike :ul :br :hr :div})

(def not-in-whitelist (html/but whitelist))

(def allowed-attrs #{:src :width :height :alt :title :href})
(defn clean-attrs
  [attrs]
  (select-keys attrs allowed-attrs))

(defn render [node] (apply str (html/emit* node)))

(defn ->extractable [url]
  (condp = (site url)
    :serious-eats (->SeriousEatsExtractable url)
    :smitten-kitchen (->SmittenKitchenExtractable url)))

(defn parse [url]
  (when-let [extractable (->extractable url)]
    {:import/url url
     :import/status :import/editing
     :import/recipe {:recipe/ingredients (or (ingredients extractable) "")
                     :recipe/notes (or (notes extractable) "")
                     :recipe/procedure (or (procedure extractable) "")
                     :recipe/title (or (title extractable) "")}
     :import/possible-images (images extractable)}))
