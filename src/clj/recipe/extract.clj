(ns recipe.extract
  (:require [org.httpkit.client :as http]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str]
            [datomic-schema.schema :as s]))

(defn host [url] (.getHost url))

(defn clean-str [s]
  (-> s
      (str/trim)
      (str/replace (-> 160 char str) " ")
      (str/replace #"\s+" " ")))

(defprotocol IExtractable
  "Something we can extract recipe data from"
  (images [this])
  (ingredients [this])
  (title [this])
  (procedure [this])
  (notes [this]))

(defrecord SmittenKitchenExtractable [html]
  IExtractable
  (images [this]
    (map
     #(first (html/attr-values % :src))
     (html/select html [:img])))
  (ingredients [this]
    (map clean-str
         (-> html
             (html/select [:li.jetpack-recipe-ingredient])
             (html/texts))))
  (title [this]
    (-> html
        (html/select [:h1.entry-title])
        (html/texts)
        (first)
        (clean-str)))
  (procedure [this]
    (-> html
        (html/select [:div.jetpack-recipe-directions])
        (html/texts)
        (str/join)
        (clean-str)))
  (notes [this]
    (-> html
        (html/select [:div.jetpack-recipe-notes])
        (html/texts)
        (str/join)
        (clean-str))))

(defrecord SeriousEatsExtractable [html]
  IExtractable
  (images [this]
    (map #(first (html/attr-values % :src))
         (html/select html [:img])))
  (ingredients [this]
    (map clean-str
         (-> html
             (html/select [:li.ingredient])
             (html/texts))))
  (title [this]
    (-> html
        (html/select [:h1.recipe-title])
        (html/texts)
        (first)
        (clean-str)))
  (procedure [this]
    (-> html
        (html/select [:div.recipe-procedure-text])
        (html/texts)
        (str/join)
        (clean-str)))
  (notes [this]
    (-> html
        (html/select [:div.recipe-introduction-body])
        (html/texts)
        (str/join)
        (clean-str))))

(def serious-eats? (partial re-find #"seriouseats\.com"))
(def smitten-kitchen?  (partial re-find #"smittenkitchen\.com"))

(defn site [url]
  (cond
    (serious-eats? url) :serious-eats
    (smitten-kitchen? url) :smitten-kitchen))

(defn html [url]
  (html/html-resource (java.net.URL. url)))

(def blacklist #{:script html/comment-node})
(def whitelist
  #{:a :b :blockquote :body :code :del :dd :dl :dt
    :em :h1 :h2 :h3 :i :img :kbd :li :ol :p :pre
    :s :sup :sub :strong :strike :ul :br :hr :div})

(def not-in-whitelist (html/but whitelist))

(def allowed-attrs #{:src :width :height :alt :title :href})
(defn clean-attrs
  [attrs]
  (select-keys attrs allowed-attrs))

(defn render [node] (apply str (html/emit* node)))

(defn body [{:keys [html]}]
  (render
   (-> html
       (html/select [:html :body])
       (html/transform [blacklist] (constantly nil))
       (html/transform [not-in-whitelist] html/text)
       (html/transform [] #(update-in % [:attrs] clean-attrs)))))

(defn ->extractable [url]
  (condp = (site url)
    :serious-eats (->SeriousEatsExtractable (html url))
    :smitten-kitchen (->SmittenKitchenExtractable (html url))))

(defn parse [url]
  (when-let [extractable (->extractable url)]
    {:import/ingredients (ingredients extractable)
     :import/notes (notes extractable)
     :import/procedure (procedure extractable)
     :import/title (title extractable)
     :import/possible-images (images extractable)
     :import/body (body extractable)}))
