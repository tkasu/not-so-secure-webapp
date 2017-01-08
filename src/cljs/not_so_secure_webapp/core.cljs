(ns not-so-secure-webapp.core
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [not-so-secure-webapp.ajax :refer [load-interceptors!]]
            [ajax.core :refer [GET POST]])
  (:import goog.History))

(enable-console-print!)

(defonce page-state (r/atom 
                     {:code-input "123456-gerbiili"
                      :price nil
                      :prices nil}))

(defn nav-link [uri title page collapsed?]
  [:li.nav-item
   {:class (when (= page (session/get :page)) "active")}
   [:a.nav-link
    {:href uri
     :on-click #(reset! collapsed? true)} title]])

(defn navbar []
  (let [collapsed? (r/atom true)]
    (fn []
      [:nav.navbar.navbar-dark.bg-primary
       [:button.navbar-toggler.hidden-sm-up
        {:on-click #(swap! collapsed? not)} "☰"]
       [:div.collapse.navbar-toggleable-xs
        (when-not @collapsed? {:class "in"})
        [:a.navbar-brand {:href "#/"} "not-so-secure-webapp"]
        [:ul.nav.navbar-nav
         [nav-link "#/" "Home" :home collapsed?]
         [nav-link "#/about" "About" :about collapsed?]
         [nav-link "#/docs" "Docs" :docs collapsed?]]]])))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     "this is the story of not-so-secure-webapp... work in progress"]]])

(defn code-response-handler [response]
  (do
     (swap! 
       page-state 
       assoc 
       :prices
       (->> response 
           :body 
           :prices 
           (map #(assoc % 
                   :checked false 
                   :id (str (:code %) "-" (:price %))))))))

(defn checkbox-click-handler [price]
  (swap! 
   page-state
   assoc
   :prices
   (map 
    (fn [item] (if (= (:id price) (:id item))
                 (assoc item :checked (not (:checked item)))
                 (assoc item :checked false)))
    (:prices @page-state))))

(defn price-chosen []
  (first (filter :checked (:prices @page-state))))

(defn home-page []
  [:div.container
   [:p "See if you're a lucky and won one our crazy prices!"]
   [:p "Insert your code below:"]
   [:form {:method "post"}
    [:input {:type "text"
             :value (:code-input @page-state)
             :on-change (do #(swap! 
                              page-state 
                              assoc 
                              :code-input 
                              (-> % .-target .-value)))}]
    [:div.inline]
    [:input {:class "btn btn-primary"
             :type "button"
             :value "check your code!"
             :onClick #(do (POST "/code"
                                 {:params {:code (:code-input @page-state)}
                                  :handler code-response-handler
                                  :response-format :json
                                  :keywords? true}))}]]
   [:div.invline]
   (when (:prices @page-state)
     [:div#price
      #_[:p "You have won: " (rand-nth (:prices @page-state))]
      [:table {:class "table table-striped"}
       [:thead
        [:tr
         [:th "Your choice"]
         [:th "Code"]
         [:th "Price"]]]
       [:tbody
        (for [price (:prices @page-state)]
          ^{:key price}
          [:tr
           [:td [:input {:type "checkbox" 
                         :value "" 
                         :checked (if (:checked price) "checked" "")
                         :onClick (do #(checkbox-click-handler price))}]]
           [:td (:code price)]
           [:td (:price price)]])]]
      [:div.inline]
      (when-let [price-to-send (price-chosen)]
        [:div.inline]
        [:form {:method "POST"}
         [:div.form-group
          [:label {:for "price"} "Your price:"]
          [:input.form-control {:type "text" :id "price" :value (:price price-to-send) :disabled true}]]
         [:div.form-group
          [:label {:for "email"} "Email address:"]
          [:input.form-control {:type "email" :id "email"}]]
         [:div.form-group
          [:label {:for "street"} "Street address:"]
          [:input.form-control {:type "text" :id "street"}]]
         [:input {:class "btn btn-primary"
             :type "button"
             :value "Redeem your price!"
             #_:onClick #_(do (POST "/code"
                                 {:handler code-response-handler
                                  :response-format :json
                                  :keywords? true}))}]])])])

(defn lum-doc-page []
  [:div.container
   (when-let [docs (session/get :docs)]
     [:div.row>div.col-sm-12
      [:div {:dangerouslySetInnerHTML
             {:__html (md->html docs)}}]])])

(def pages
  {:home #'home-page
   :about #'about-page
   :docs #'lum-doc-page})

(defn page []
  [(pages (session/get :page))])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

(secretary/defroute "/about" []
  (session/put! :page :about))

(secretary/defroute "/docs" []
  (session/put! :page :docs))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
        (events/listen
          HistoryEventType/NAVIGATE
          (fn [event]
              (secretary/dispatch! (.-token event))))
        (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(session/put! :docs %)}))

(defn mount-components []
  (r/render [#'navbar] (.getElementById js/document "navbar"))
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
