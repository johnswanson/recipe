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

(defn ring-handler [{:keys [db github]}]
  (-> (routes
       (GET "/" [] (index))
       (GET "/cards" [] (cards))
       (GET "/logout" [] logout)
       (GET "/callback" {{:keys [code]} :params
                         :as req}
         (let [access-token (recipe.github/access-token github code)
               user (recipe.github/user github access-token)]
           (recipe.datomic/add-or-update-user! (:conn db) user access-token)
           {:status 307
            :headers {"Location" "/"}
            :body ""
            :session (assoc (:session req) :uid (:login user))})))
      (wrap-restful-format)))

(def site (-> site-defaults (assoc-in [:static :resources] "/public")))

(defmulti read (fn [x & args] x))
(defmethod read [:github/auth-url] [_ & [{:keys [github]}]] (recipe.github/auth-url github))
(defmethod read [:app/logged-in-user] [_ & [{:keys [ring-req db]}]]
  (let [uid (get-in ring-req [:session :uid])]
    (recipe.datomic/get-user (:conn db) uid)))

(defmulti handle-event #(first (:event %)))

(defmethod handle-event :default [{:keys [?reply-fn event]}]
  (when ?reply-fn (?reply-fn {:unhandled-event event})))

(defmethod handle-event :app/query [ev-msg]
  (let [{:keys [event id ?data ring-req ?reply-fn send-fn db]} ev-msg
        [_ query] event]
    (when (and ?reply-fn query)
      (?reply-fn (read query ev-msg)))))

(defn wrap-db
  "Sorta-middleware for the ev-msg, just adds the `db` to the ev-msg."
  [handler db]
  (fn [ev-msg]
    (handler (assoc ev-msg :db db))))

(defn wrap-github
  [handler github]
  (fn [ev-msg]
    (handler (assoc ev-msg :github github))))

(defn sente-handler [{:keys [db github]}]
  (-> handle-event
      (wrap-db db)
      (wrap-github github)))

