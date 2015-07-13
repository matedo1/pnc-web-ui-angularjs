/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

(function() {

  var module = angular.module('pnc.project', [
    'ui.router',
    'pnc.common.restclient',
    'angularUtils.directives.uiBreadcrumbs'
  ]);

  module.config(['$stateProvider', function($stateProvider) {

    $stateProvider.state('project', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
          //templateUrl: 'common/templates/single-col-center.tmpl.html'
        }
      },
      data: {
        proxy: 'project.list'
      }
    });

   $stateProvider.state('project.list', {
      url: '/project',
      templateUrl: 'project/views/project.list.html',
      data: {
        displayName: 'Projects'
      },
      controller: 'ProjectListController',
      controllerAs: 'listCtrl',
      resolve: {
        restClient: 'PncRestClient',
        projectList: function(restClient) {
          return restClient.Project.query().$promise;
        }
      },
    });

  $stateProvider.state('project.detail', {
    url: '/project/{projectId:int}',
    templateUrl: 'project/views/project.detail.html',
    data: {
       displayName: '{{ projectDetail.name }}',
    },
    controller: 'ProjectDetailController',
    controllerAs: 'detailCtrl',
    resolve: {
      restClient: 'PncRestClient',
      projectDetail: function(restClient, $stateParams) {
        return restClient.Project.get({
          projectId: $stateParams.projectId}).$promise;
      },
      projectConfigurationList: function(restClient, $stateParams) {
        return restClient.Configuration.getAllForProject({
          projectId: $stateParams.projectId}).$promise;
      },
    }
  });

  }]);

})();
