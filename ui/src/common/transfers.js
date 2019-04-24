import axios from 'axios';
import {bffBaseURL} from '@/common/config';
import {activePortfolio} from '@/common/portfolio';

const transfer = axios.create({
  baseURL: bffBaseURL + '/api/transfer',
  
})

export function create(request) {
  return transfer.post(request);
}

export function deposit(amount, accountType, accountId) {
  let request = {
    destinationType: "portfolio",
    destinationId: activePortfolio.id,
    sourceType: accountType,
    sourceId: accountId,
    amount: amount
  };
  return transfer.post('', request);
}

export function submitTransfer(transferRequest) {
  let formData = new FormData();
  if (transferRequest.depositWithdrawl === "portfolioWithdrawl") {
    formData.append('destinationType', transferRequest.accountType);
    formData.append('destinationId', transferRequest.accountId);
    formData.append('sourceType', 'portfolio');
    formData.append('sourceId', activePortfolio.id);
    formData.append('amount', transferRequest.amount);
  } else {
    formData.append('destinationType', 'portfolio');
    formData.append('destinationId', activePortfolio.id);
    formData.append('sourceType', transferRequest.accountType);
    formData.append('sourceId', transferRequest.accountId);
    formData.append('amount', transferRequest.amount);
  }

  return axios({
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    url: bffBaseURL + '/api/transfer',
    data: formData
  });
}