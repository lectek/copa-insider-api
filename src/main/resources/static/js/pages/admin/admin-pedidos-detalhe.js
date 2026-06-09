(function () {
  const root = document.querySelector('[data-cancel-order-root]');
  if (root) {
    const startButton = root.querySelector('[data-cancel-start]');
    const cancelForm = root.querySelector('[data-cancel-form]');
    const reasonSelect = root.querySelector('[data-cancel-reason]');
    const resetButton = root.querySelector('[data-cancel-reset]');

    if (startButton && cancelForm && reasonSelect && resetButton) {
      const resetForm = function () {
        cancelForm.hidden = true;
        reasonSelect.value = '';
        startButton.hidden = false;
      };

      startButton.addEventListener('click', function () {
        const confirmed = window.confirm(
          'Tem certeza que deseja cancelar este pedido?'
        );
        if (!confirmed) {
          return;
        }

        startButton.hidden = true;
        cancelForm.hidden = false;
        reasonSelect.focus();
      });

      resetButton.addEventListener('click', resetForm);
    }
  }

  const statusForm = document.querySelector('[data-order-status-form]');
  if (!statusForm) {
    return;
  }

  const statusSelect = statusForm.querySelector('[data-order-status-select]');
  const paymentPanel = statusForm.querySelector('[data-payment-receipt-panel]');
  const defaultActions = statusForm.querySelector('[data-default-status-actions]');

  if (!statusSelect || !paymentPanel || !defaultActions) {
    return;
  }

  const syncPaymentPanel = function () {
    const isPaid = statusSelect.value === 'PAGO';
    paymentPanel.hidden = !isPaid;
    defaultActions.hidden = isPaid;
  };

  statusSelect.addEventListener('change', syncPaymentPanel);
  syncPaymentPanel();
})();
