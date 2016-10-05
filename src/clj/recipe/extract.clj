(ns recipe.extract
  (:require [org.httpkit.client :as http]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str]))

(defn smitten-kitchen-previous [html]
  (first
   (html/attr-values
    (first (html/select html [[:a (html/attr= :rel "prev")]]))
    :href)))

(defn images [html]
  (map
   #(html/attr-values % :src)
   (html/select html [:img])))

(defn html [url]
  (html/html-resource (java.net.URL. url)))

(defn ingredients [html]
  (map html/text (html/select html [:li.jetpack-recipe-ingredient])))

(defn notes [html]
  (str/join (map html/text (html/select html [:div.jetpack-recipe-notes]))))

(defn procedure [html]
  (str/join (map html/text (html/select html [:div.jetpack-recipe-directions]))))

(defn title [html]
  (html/text (first (html/select html [:h1.entry-title]))))

(defn remove-nbsp [str] (str/replace str (-> 160 char str) " "))


(defn parse [url]
  (let [html (html url)]
    {:ingredients (ingredients html)
     :notes (notes html)
     :procedure (procedure html)
     :title (title html)
     :possible-images (images html)}))
