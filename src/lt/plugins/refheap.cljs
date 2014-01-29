(ns lt.plugins.refheap
  (:require [lt.object :as object]
            [lt.objs.editor :as editor]
            [lt.objs.editor.pool :as pool]
            [lt.objs.tabs :as tabs]
            [lt.objs.popup :as popup]
            [lt.objs.command :as cmd]
            [ajax.core :refer [POST]])
  (:require-macros [lt.macros :refer [defui behavior]]))


(defn post-done [response]
  (let [url (get response "url")]
    (popup/popup! {:body [:span "Posted to " [:a {:href url} url]] :buttons [{:label "OK"}]})))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "Refheap error: " status " " status-text)))

(defn post-to-refheap []
  (let [ed (pool/last-active)
        selection (editor/selection ed)
        pos (editor/->cursor ed "start")
        endpoint "https://www.refheap.com/api/paste"
        text (if (seq selection)
               selection
               (do (editor/select-all ed)
                   (editor/selection ed)))]
    ; restore selection
    (editor/set-selection ed pos pos)
    (POST endpoint
          {:params {:contents text
                    :language ".clj"}
           :format :raw
           :error-handler error-handler
           :handler post-done})))

(cmd/command {:command ::post-to-refheap
              :desc "refheap: Post to refheap"
              :exec (fn []
                      (post-to-refheap))})
