(ns recipe.github
  (:require [oauth.github :as github]
            [environ.core :refer [env]]
            [taoensso.timbre :as log]
            [com.stuartsierra.component :as component]))

(defrecord GithubAPI [config]
  component/Lifecycle
  (start [component]
    (let [{:keys [client-id client-secret callback-uri]} config
          access-token #(:access-token (github/oauth-access-token client-id client-secret % callback-uri))
          client #(github/oauth-client %)
          auth-url (github/oauth-authorization-url client-id callback-uri)]
      (-> component
          (assoc :f-access-token access-token)
          (assoc :f-client client)
          (assoc :auth-url auth-url))))
  (stop [component]
    (dissoc component :f-access-token :f-client :auth-url)))

(defn new-api [config]
  (->GithubAPI config))

(defn get-user [client]
  (client
   {:method :get
    :url "https://api.github.com/user"}))

(defn access-token [{:keys [f-access-token] :as api} code]
  (f-access-token code))

(defn client [{:keys [f-client] :as api} access-token]
  (f-client access-token))

(defn user [api access-token]
  (when-let [client (client api access-token)]
    (get-user client)))

(defn auth-url [api] (:auth-url api))
