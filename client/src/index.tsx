import React from 'react';
import ReactDOM from 'react-dom';
import 'bootstrap/dist/css/bootstrap.css';

import { Provider } from 'react-redux';
import { store } from './store/store';

import './index.css';
import App from './App';
import * as serviceWorker from './serviceWorker';
import { updateServiceworker } from '../src/reducers/serviceWorker';
import createHistory from 'history/createBrowserHistory';
import httpService from './api/axiosInterceptor';

const history = createHistory();
httpService.setupInterceptors(store, history);

ReactDOM.render(
  <Provider store={store}>
    <App />
  </Provider>,
  document.getElementById('root')
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: http://bit.ly/CRA-PWA
// serviceWorker.register({ onUpdate: (registration)=> 
//                                             {  
//                                              store.dispatch(updateServiceworker())
//                                             }
//                       });
serviceWorker.register()