(ns not-so-secure-webapp.core
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [not-so-secure-webapp.ajax :refer [load-interceptors!]]
            [ajax.core :refer [GET POST DELETE PUT]])
  (:import goog.History))

(enable-console-print!)

(defonce page-state (r/atom 
                     {:code-input "123456-gerbiili"
                      :price nil
                      :prices nil
                      :winners nil
                      :user {:email ""
                             :address ""
                             :id ""
                             :password ""}
                      :login-err nil
                      :code-err nil
                      :prices-all nil
                      :new-code nil
                      :new-price nil}))

(defn reset-page-state! []
  (reset! page-state {:code-input "123456-gerbiili"
                      :price nil
                      :prices nil
                      :winners nil
                      :user {:email ""
                             :address ""
                             :id ""
                             :password ""}
                      :login-err nil
                      :code-err nil
                      :prices-all nil
                      :new-code nil
                      :new-price nil}))

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
         [nav-link "#/winners" "Winners" :winners collapsed?]
         ;[nav-link "#/about" "About" :about collapsed?]
         ;[nav-link "#/docs" "Docs" :docs collapsed?]
         [nav-link "#/signin" "Admin" :docs collapsed?]]]])))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     "this is the story of not-so-secure-webapp... work in progress"]]])

(defn code-response-handler [response]
  (let [prices (->> response :body :prices)] 
    (do
      (if (empty? prices)
        (swap! 
         page-state 
         assoc 
         :code-err 
         (str "Sorry! No prices for code: " (:code-input @page-state)))
        (swap! page-state assoc :code-err nil))
      (swap! 
       page-state 
       assoc 
       :prices
       (->> prices
            (map #(assoc % 
                    :checked false 
                    :id (str (:code %) "-" (:price %)))))))))

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

(defn winner-response-handler [response]
  (do
     (swap! 
       page-state 
       assoc 
       :winners
       (->> response 
           :body 
           :winners 
           )))
  (print @page-state))

(defn prices-response-handler [response]
  (let [prices (->> response :body :prices)] 
    (do
      (swap! 
       page-state 
       assoc 
       :prices-all
       prices)))
  (print @page-state))

(defn redeem-response-handler [response]
  (do
    (reset-page-state!)
    (session/put! :page :winners)))

(defn signin-response-handler [response]
  (do
    (swap! page-state assoc :login-err nil)
    (session/put! :page :admin)))

(defn signin-response-err-handler [response]
  (do
    (swap! page-state assoc :login-err "Incorrect user and/or password!")))

(defn delete-response-handler [response]
  (do
    (GET "/prices"
         {:params nil
          :handler prices-response-handler
          :keywords? true})))

(defn put-response-handler [response]
  (do
    (GET "/prices"
         {:params nil
          :handler prices-response-handler
          :keywords? true})
    (swap! page-state assoc :new-code nil)
    (swap! page-state assoc :new-price nil)))

(defn price-chosen []
  (first (filter :checked (:prices @page-state))))

(defn admin-page []
  [:div.container
   [:div.row]
   [:form {:method "get"}
    [:input {:class "btn btn-primary"
             :type "button"
             :value "Get codes and prices!"
             :onClick #(do (GET "/prices"
                                 {:params nil
                                  :handler prices-response-handler
                                  :keywords? true}))}]]
   (when-let  [prices (:prices-all @page-state)]
     [:div.inline]
     [:p (:prices @page-state)]
     [:div#price
      [:table {:class "table table-striped"}
       [:thead
        [:tr
         [:th "Code"]
         [:th "Price"]
         [:th "Delete?"]]]
       [:tbody
        (for [price prices]
          ^{:key price}
          [:tr
           ; This is a bit forced, I know.
           [:td (:code price)]
           [:td (:price price)]
           [:td [:button {:type "button" 
                          :class "btn btn-danger"
                          :onClick #(do (DELETE "/price"
                                                {:params {:price (:price price)
                                                          :code (:code price)}
                                                 :handler delete-response-handler
                                                 :keywords? true}))}]]])]]])
   [:div.inline]
   [:form {:method "PUT"}
         [:div.form-group
          [:label {:for "code"} "Code"]
          [:input.form-control 
           {:type "text" 
            :id "code"
            :value (:new-code @page-state)
            :on-change (do #(swap! 
                              page-state 
                              assoc
                              :new-code
                              (-> % .-target .-value)))}
           ]]
         [:div.form-group
          [:label {:for "price"} "Price"]
          [:input.form-control 
           {:type "text" 
            :id "price"
            :value (:new-price @page-state)
            :on-change (do #(swap! 
                              page-state 
                              assoc
                              :new-price
                              (-> % .-target .-value)))}]]
         [:input {:class "btn btn-primary"
             :type "button"
             :value "Add new price!"
             :onClick (do #(PUT "/price"
                                 {:params {:code (:new-code @page-state)
                                           :price (:new-price @page-state)}
                                  :handler put-response-handler
                                  :response-format :json
                                  :keywords? true}))}]]])

(defn signin-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:h2 "Login admin"]]]
   [:form {:method "POST"}
    [:div.form-group
     [:label {:for "id"} "username"]
     [:input.form-control 
      {:type "text" 
       :id "id"
       :value (get-in @page-state [:user :id])
       :on-change (do #(swap! 
                        page-state 
                        assoc-in
                        [:user :id]
                        (-> % .-target .-value)))}
      ]]
    [:div.form-group
     [:label {:for "password"} "password"]
     [:input.form-control 
      {:type "password" 
       :id "password"
       :value (get-in @page-state [:user :password])
       :on-change (do #(swap! 
                        page-state 
                        assoc-in
                        [:user :password]
                        (-> % .-target .-value)))}]]
    [:input {:class "btn btn-primary"
             :type "button"
             :value "Log In!"
             :onClick (do 
                        #(POST "/signin"
                               {:params {:id (get-in @page-state [:user :id])
                                         :password (get-in @page-state [:user :password])}
                                :handler signin-response-handler
                                :error-handler signin-response-err-handler
                                :keywords? true}))}]]
   [:br]
   (when-let [err-msg (:login-err @page-state)]
     [:div {:class "alert alert-danger"} err-msg])])

(defn winners-page []
  [:div.container
   [:form {:method "get"}
    [:input {:class "btn btn-primary"
             :type "button"
             :value "Check the past winners!"
             :onClick #(do (GET "/winners"
                                 {:params nil
                                  :handler winner-response-handler
                                  :keywords? true}))}]]
   [:p "Past winners:"]
   (when-let  [winners (:winners @page-state)]
     [:div.inline]
     [:p (:prices @page-state)]
     [:div#price
      [:table {:class "table table-striped"}
       [:thead
        [:tr
         [:th "Email"]
         [:th "Address"]
         [:th "Price"]]]
       [:tbody
        (for [winner winners]
          ^{:key winner}
          [:tr
           ; This is a bit forced, I know.
           [:td {:dangerouslySetInnerHTML {:__html (:email winner)}}]
           [:td {:dangerouslySetInnerHTML {:__html (:address winner)}}]
           [:td (:price winner)]])]]])])

(defn home-page []
  [:div.container
   [:p "See if you're a lucky and won one our crazy prices!"]
   [:p "Insert your code below:"]
   (when-let [err-msg (:code-err @page-state)]
     [:div {:class "alert alert-danger"} err-msg])
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
             :onClick (do 
                        #(POST "/code"
                               {:params {:code (:code-input @page-state)}
                                :handler code-response-handler
                                :response-format :json
                                :keywords? true}))}]]
   [:div.invline]
   (when (:prices @page-state)
     [:div#price
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
          [:input.form-control 
           {:type "text" :id "price" :value (:price price-to-send) :disabled true}]]
         [:div.form-group
          [:label {:for "email"} "Email address:"]
          [:input.form-control 
           {:type "email" 
            :id "email"
            :value (get-in @page-state [:user :email])
            :on-change (do #(swap! 
                              page-state 
                              assoc-in
                              [:user :email]
                              (-> % .-target .-value)))}
           ]]
         [:div.form-group
          [:label {:for "street"} "Street address:"]
          [:input.form-control 
           {:type "text" 
            :id "street"
            :value (get-in @page-state [:user :address])
            :on-change (do #(swap! 
                              page-state 
                              assoc-in
                              [:user :address]
                              (-> % .-target .-value)))}]]
         [:input {:class "btn btn-primary"
             :type "button"
             :value "Redeem your price!"
             :onClick (do #(POST "/redeem"
                                 {:params {:code (:code price-to-send)
                                           :price (:price price-to-send)
                                           :email (get-in @page-state [:user :email])
                                           :address (get-in @page-state [:user :address])}
                                  :handler redeem-response-handler
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
   :winners #'winners-page
   :about #'about-page
   :docs #'lum-doc-page
   :signin #'signin-page
   :admin #'admin-page})

(defn page []
  [(pages (session/get :page))])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

(secretary/defroute "/winners" []
  (session/put! :page :winners))

(secretary/defroute "/about" []
  (session/put! :page :about))

(secretary/defroute "/docs" []
  (session/put! :page :docs))

(secretary/defroute "/signin" []
  (session/put! :page :signin))

(secretary/defroute "/admin" []
  (session/put! :page :admin))

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
