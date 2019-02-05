import axios from 'axios';
import {bffBaseURL} from '@/common/config';
import qs from 'qs';

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
    url: bffBaseURL + '/api/portfolio',
    data: formData
  }).then((response) => {    
    const portfolioId = response.data.portfolioId;
    activePortfolio.id = portfolioId;
    activePortfolio.name = request.name;
    return portfolioId;
  });
}

export function load(portfolioId) {
  const request = axios.get(
    bffBaseURL + '/api/portfolio/' + portfolioId + '/summary');
  request.then(response => {
    activePortfolio.id = response.data.portfolioId;
    activePortfolio.name = response.data.name;
  });
  return request;
}