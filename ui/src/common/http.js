import axios from 'axios';
import {iexPublicKey} from '@/common/config';

export const IEX = axios.create({
  baseURL: 'https://sandbox.iexapis.com/stable/',
  params: {
    token: iexPublicKey
  },
});
