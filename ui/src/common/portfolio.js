import axios from 'axios';
import {bffBaseURL} from '@/common/config';

const baseUrl = bffBaseURL + '/api/portfolio';
const portfolioUrl = () => baseUrl + '/' + activePortfolio.id;

export let activePortfolio = {
  state: {
    id: window.sessionStorage.portfolioId,
    name: window.sessionStorage.portfolioName
  },
  get id() { 
    return this.state.id;
  },
  set id(portfolioId) {
    window.sessionStorage.portfolioId = portfolioId;
    this.state.id = portfolioId;
  },
  get name() { 
    return this.state.name;
  },
  set name(name) {
    window.sessionStorage.portfolioName = name;
    this.state.name = name;
  },
  clear() {
    this.id = null;
    this.name = null;
  }
}
export function open(request) {
  const formData = new FormData();
  formData.append('name', request.name);

  return axios({
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    url: baseUrl,
    data: formData
  }).then((response) => {    
    const portfolioId = response.data.portfolioId;
    activePortfolio.id = portfolioId;
    activePortfolio.name = request.name;
    return portfolioId;
  });
}

export function getPortfolio(options) {
  const {includeOrders, includeSharePrices} = options || {};
  const portfolioId = 
    options && options.portfolioId 
      ? options.portfolioId 
      : activePortfolio.id;
  
  const url = new URL('/api/portfolio/' + portfolioId + '/summary', baseUrl);
  if (includeOrders) url.searchParams.append('includeOrderInfo', true);
  if (includeSharePrices) url.searchParams.append('includePrices', true);
  const request = axios.get(url.toString());
  return request.then(response => {
      activePortfolio.id = response.data.portfolioId;
      activePortfolio.name = response.data.name;
    return response.data;      
  })
  .catch(err => {
    activePortfolio.clear();
  });
}

export function getDetails(portfolioId) {
  if (portfolioId == null) { portfolioId = activePortfolio.id; }
  const request = axios.get(baseUrl + '/' + portfolioId);
  return request.then(response => response.data);
}

export function placeOrder(order) {
  const portfolioId = activePortfolio.id;
  const formData = new FormData();
  formData.append('symbol', order.symbol);
  formData.append('shares', order.shares);
  formData.append('order', order.tradeType);
  
  return axios({
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      url: portfolioUrl() + '/order',
      data: formData
  }); 
}