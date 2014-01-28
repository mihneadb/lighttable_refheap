(ns lt.plugins.refheap
  (:require [lt.object :as object]
            [lt.objs.editor :as editor]
            [lt.objs.editor.pool :as pool]
            [lt.objs.tabs :as tabs]
            [lt.objs.command :as cmd]
            [ajax.core :refer [POST]])
  (:require-macros [lt.macros :refer [defui behavior]]))

(defui hello-panel [this]
  [:h1 "Hello from refheap"])

(object/object* ::refheap.hello
                :tags [:refheap.hello]
                :name "refheap"
                :init (fn [this]
                        (hello-panel this)))

(behavior ::on-close-destroy
          :triggers #{:close}
          :reaction (fn [this]
                      (when-let [ts (:lt.objs.tabs/tabset @this)]
                        (when (= (count (:objs @ts)) 1)
                          (tabs/rem-tabset ts)))
                      (object/raise this :destroy)))


(cmd/command {:command ::say-hello
              :desc "refheap: Say Hello"
              :exec (fn []
                      (.log js/console "yo"))})



(defn post-done [response]
  (.log js/console (str response)))

(defn post-to-refheap []
  (let [ed (pool/last-active)
        text (editor/selection ed)
        endpoint "https://www.refheap.com/api/paste"]
    (POST endpoint
          {:params {:contents text
                    :language "clj"}
           :handler post-done})))


(cmd/command {:command ::post-to-refheap
              :desc "refheap: Post to refheap"
              :exec (fn []
                      (post-to-refheap))})
