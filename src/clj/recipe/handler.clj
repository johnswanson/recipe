(ns recipe.handler
  (:require [ring.middleware.defaults :refer [site-defaults]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [compojure.core :refer [routes GET]]
            [compojure.route :as route]
            [clojure.core.match :as match :refer [match]]
            [recipe.datomic]
            [recipe.github]
            [hiccup.page :refer [html5]]))

(def logout
  {:status 307
   :headers {"Location" "/"}
   :body ""
   :session nil})

(defn index []
  (html5
   [:html {:lang "en"}
    [:head
     [:meta {:charset "utf-8"}]
     [:link {:href "http://fonts.googleapis.com/css?family=Roboto:400,300,200"
             :rel "stylesheet"
             :type "text/css"}]
     [:link {:rel "stylesheet" :href "/css/app.css"}]]
    [:body
     [:div#app]
     [:script {:src "/js/compiled/app.js"}]
     [:script {:src "https://use.fontawesome.com/efa7507d6f.js"}]]]))

(defn cards []
  (html5
   [:html {:lang "en"}
    [:head
     [:meta {:charset "utf-8"}]
     [:link {:href "http://fonts.googleapis.com/css?family=Roboto:400,300,200"
             :rel "stylesheet"
             :type "text/css"}]
     [:link {:rel "stylesheet" :href "/css/app.css"}]]
    [:body
     [:div#app]
     [:script {:src "/js/compiled_cards/app.js"}]
     [:script {:src "https://use.fontawesome.com/efa7507d6f.js"}]]]))

(defn ring-handler [{:keys [db]}]
  (-> (routes
       (GET "/" [] (index))
       (GET "/cards" [] (cards))
       (GET "/logout" [] logout)
       (GET "/callback" {{:keys [code]} :params
                         :as req}
         (let [access-token (recipe.github/access-token code)
               user (recipe.github/user access-token)]
           (recipe.datomic/add-or-update-user! (:conn db) user access-token)
           {:status 307
            :headers {"Location" "/"}
            :body ""
            :session (assoc (:session req) :uid (:login user))})))
      (wrap-restful-format)))

(def site (-> site-defaults (assoc-in [:static :resources] "/public")))

(defmulti handle-event #(first (:event %)))

(defmethod handle-event :default [{:keys [?reply-fn event]}]
  (when ?reply-fn (?reply-fn {:unhandled-event event})))

(defmulti read first)
(defmethod read :github/auth-url [_] recipe.github/auth-url)

(defmethod handle-event :app/query [ev-msg]
  (let [{:keys [event id ?data ring-req ?reply-fn send-fn]} ev-msg
        [id query] event]
    (when (and ?reply-fn query)
      (?reply-fn (read query)))))

(defn sente-handler [{:keys [db]}]
  (fn [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
    (let [session (:session ring-req)
          headers (:headers ring-req)
          uid (:uid session)]
      (println "Session: " session)
      (handle-event ev-msg))))

