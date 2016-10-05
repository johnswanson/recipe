(ns cards.events
  (:require [cljs.test :refer [testing is]]
            [devcards.core :as devcards]
            [recipe.events :refer [success-get-user]]
            [re-frame.core :refer [dispatch dispatch-sync console]]
            [re-frame.db]))

(devcards/deftest a-test
  "# We can get a user"
  (testing
    (let [before {}
          user {:user/id 123}]
      (is (= (success-get-user {} [user])
             {:recipe.db/logged-in-user user})))))


(devcards/deftest testing-editor
  "# How does the editor work?"
  (testing "I can edit a recipe field"
    (let [init-db {}]
      (reset! re-frame.db/app-db init-db)
      (dispatch-sync [:update-editing-recipe :recipe/title "hello"])
      (is (= @re-frame.db/app-db {:recipe.db/editing-recipe {:recipe/id -1 :recipe/title "hello"}}))

      ;; for this one, we don't emit the real event
      (is (= (:db (recipe.events/save-recipe {:db @re-frame.db/app-db} nil))
             {:recipes/by-id {-1 {:recipe/id -1
                                  :recipe/title "hello"}}
              :recipe.db/recipes [-1]})))))

