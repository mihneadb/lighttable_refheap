(ns lt.plugins.refheap
  (:require [lt.object :as object]
            [lt.objs.editor :as editor]
            [lt.objs.editor.pool :as pool]
            [lt.objs.files :as files]
            [lt.objs.tabs :as tabs]
            [lt.objs.popup :as popup]
            [lt.objs.command :as cmd]
            [ajax.core :refer [POST]])
  (:require-macros [lt.macros :refer [defui behavior]]))


(defui link [url]
  [:a {:href url} url]
  :click (fn []
           (.Shell.openExternal (js/require "nw.gui") url)))


(defn post-done [response]
  (let [url (get response "url")]
    (popup/popup! {:body [:span "Posted to " (link url)] :buttons [{:label "OK"}]})))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "Refheap error: " status " " status-text)))

(defn post-to-refheap []
  (let [ed (pool/last-active)
        selection (editor/selection ed)
        pos (editor/->cursor ed "start")
        endpoint "https://www.refheap.com/api/paste"
        ext (-> @ed :info :name files/ext)
        text (if (seq selection) ; if nothing is selected, send the whole file
               selection
               (do (editor/select-all ed)
                   (editor/selection ed)))]
    ; restore selection
    (editor/set-selection ed pos pos)
    (POST endpoint
          {:params {:contents text
                    :language ext}
           :format :raw
           :error-handler error-handler
           :handler post-done})))


(cmd/command {:command ::post-to-refheap
              :desc "Refheap: Post to Refheap"
              :exec (fn []
                      (post-to-refheap))})
