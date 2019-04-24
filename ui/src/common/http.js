import axios from 'axios';

export const IEX = axios.create({
  baseURL: 'https://api.iextrading.com/1.0/'
})