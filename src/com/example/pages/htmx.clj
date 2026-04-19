(ns com.example.pages.htmx
  (:require [com.example.ui :as ui]))

(defn page [_req state]
  (let [{:keys [items settings]} @state]
    (ui/render-html
     [:html
      [:head
       [:title "HTMX + Idiomorph"]
       [:script {:src "https://unpkg.com/htmx.org@2.0.4"
                 :crossorigin "anonymous"}]
       [:script {:src "https://unpkg.com/idiomorph@0.3.0/dist/idiomorph-ext.min.js"
                 :crossorigin "anonymous"}]]
      [:body {:hx-boost "true"
              :hx-ext "morph"
              :hx-swap "morph:innerHTML"
              :style "font-family: sans-serif; padding: 20px;"}
       (ui/nav-bar)
       [:h1 "HTMX + Idiomorph"]
       [:form {:method "post"}
        (ui/items-table {:items items
                         :action-prefix "/htmx"
                         :save-action "/htmx/save"})
        (ui/settings-form {:settings settings
                           :action "/htmx/settings"})]]])))

(defn save! [req state]
  (let [params (:params req)]
    (swap! state update :items
           (fn [items]
             (vec (for [i (range (count items))]
                    (assoc (nth items i) :quantity
                           (ui/parse-int (get params (str "qty-" i))))))))
    (ui/redirect "/htmx")))

(defn increment! [req state]
  (let [idx (ui/parse-int (get-in req [:params "index"]))]
    (swap! state update-in [:items idx :quantity] inc)
    (ui/redirect "/htmx")))

(defn decrement! [req state]
  (let [idx (ui/parse-int (get-in req [:params "index"]))]
    (swap! state update-in [:items idx :quantity] dec)
    (ui/redirect "/htmx")))

(defn save-settings! [req state]
  (let [params (:params req)]
    (swap! state assoc :settings
           {:text-field (get params "text-field" "")
            :select-field (get params "select-field" "option-a")
            :date-field (get params "date-field" "")})
    (ui/redirect "/htmx")))
