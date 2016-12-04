(ns recipe.util)

(defn namespaced-map [m ns]
  (into {} (map (fn [[k v]]
                  [(keyword ns (name k))
                   v])
                m)))
