(() => {
  const fileInput = document.querySelector("[data-avatar-file-input='true']");
  const fileName = document.querySelector("[data-avatar-file-name='true']");
  const preview = document.querySelector("[data-avatar-preview='true']");

  if (!fileInput || !fileName || !preview) {
    return;
  }

  function resetPreview() {
    fileName.textContent = "Nenhum arquivo selecionado.";
  }

  fileInput.addEventListener("change", () => {
    const file = fileInput.files?.[0];
    if (!file) {
      resetPreview();
      return;
    }

    fileName.textContent = `${file.name} - ${Math.round(file.size / 1024)} KB`;
    if (!file.type.startsWith("image/")) {
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      if (typeof reader.result === "string") {
        preview.src = reader.result;
      }
    };
    reader.readAsDataURL(file);
  });
})();
