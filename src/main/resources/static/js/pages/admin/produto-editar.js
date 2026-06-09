// pages/produto-editar.js
function getCsrf() {
  const token = document.querySelector('meta[name="_csrf"]')?.content;
  const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
  return { token, header };
}

function withCsrf(init = {}) {
  const { token, header } = getCsrf();
  init.headers = Object.assign({}, init.headers || {}, token ? { [header]: token } : {});
  return init;
}

function toast(msg, type = 'ok') {
  let box = document.getElementById('toast');
  if (!box) {
    box = document.createElement('div');
    box.id = 'toast';
    document.body.appendChild(box);
  }
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.textContent = msg;
  box.appendChild(el);
  setTimeout(() => el.remove(), 3500);
}

function normalizeErrorMessage(raw, fallback) {
  const text = String(raw || '')
    .replace(/<[^>]*>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
  return text || fallback;
}

function parseJsonSafely(raw) {
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

function parseApiErrorMessage(raw, fallback) {
  const payload = parseJsonSafely(raw);
  const message = payload?.message || payload?.error || payload?.detail || raw;
  return normalizeErrorMessage(message, fallback);
}

function isLikelyHtml(contentType, raw) {
  return String(contentType || '').includes('text/html')
    || /<html[\s>]/i.test(String(raw || ''))
    || /<body[\s>]/i.test(String(raw || ''));
}

function isSessionRedirect(response, contentType, raw) {
  const redirectedToLogin = response?.redirected
    && String(response?.url || '').includes('/auth/login');
  const loginForm = /name=["']username["']/i.test(String(raw || ''))
    && /\/auth\/login/i.test(String(raw || ''));
  return redirectedToLogin || (isLikelyHtml(contentType, raw) && loginForm);
}

async function responseToUploadError(response, fallback) {
  const raw = await response.text();
  const message = parseApiErrorMessage(raw, fallback);
  const requestRef = response.headers?.get('x-railway-request-id')
    || response.headers?.get('x-correlation-id')
    || '';
  return requestRef ? `${message} (ref ${requestRef})` : message;
}

function isRetryableUploadStatus(status) {
  return status === 502 || status === 503 || status === 504;
}

function resolveProdutoId(raw) {
  const id = String(raw ?? '').trim();
  return /^\d+$/.test(id) ? id : null;
}

const $ = (s) => document.querySelector(s);

const $id = $('#id');
const $nome = $('#nome');
const $descricao = $('#descricao');
const $preco = $('#preco');
const $imagem = $('#imagem');
const $imagemPreview = $('#imagem-preview');
const $imagemGaleria = $('#imagem-galeria');
const $imagemArquivoCamera = $('#imagemArquivoCamera');
const $imagemArquivo = $('#imagemArquivo');
const $categoria = $('#categoria');
const $estoque = $('#estoque');
const $alertaEstoqueLimite = $('#alertaEstoqueLimite');
const $perfilVenda = $('#perfilVenda');
const $perfilVendaHint = $('#perfilVendaHint');
const $codigoBarras = $('#codigoBarras');
const $disponivel = $('#disponivel');
const $validador = $('#validador');
const $fiscalNcm = $('#fiscalNcm');
const $fiscalCest = $('#fiscalCest');
const $fiscalCfop = $('#fiscalCfop');
const $fiscalOrigem = $('#fiscalOrigem');
const $fiscalIcmsCst = $('#fiscalIcmsCst');
const $fiscalCsosn = $('#fiscalCsosn');
const $fiscalPisCst = $('#fiscalPisCst');
const $fiscalCofinsCst = $('#fiscalCofinsCst');

const $status = $('#status');
const $btnExcluir = $('#btn-excluir');
const $btnExcluirSecundario = $('#btn-excluir-secundario');
const $btnSalvar = $('#btn-salvar');
const $btnGerarIA = $('#btn-gerar-ia');
const $btnUploadImagem = $('#btn-upload-imagem');
const $btnValidar = $('#btn-validar');
const $btnPublicar = $('#btn-publicar');
const imageInputs = [$imagemArquivoCamera, $imagemArquivo].filter(Boolean);
let imageUploadInProgress = false;
let isSaving = false;
let isWorkflowActionInProgress = false;
let galleryImages = [];
const MAX_UPLOAD_IMAGE_BYTES = 20 * 1024 * 1024;
const ALLOWED_UPLOAD_IMAGE_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);

const ALERTA_ESTOQUE_MIN = 2;
const ALERTA_ESTOQUE_MAX = 100000;
const ALERTA_ESTOQUE_PADRAO = 2;
const PERFIL_VENDA_MULTIPLIERS = Object.freeze({
  LENTA: 0.6,
  NORMAL: 1,
  RAPIDA: 1.8
});

async function carregarProdutoSeNecessario() {
  // Se ja veio do SSR, nao precisa. Mantemos apenas como fallback
  const id = $id?.value;
  if (!id) return;
  if ($nome?.value) return; // ja tem dados do SSR

  try {
    const r = await fetch(`/api/admin/produtos/${id}`);
    if (!r.ok) return;
    const p = await r.json();
    fill(p);
  } catch (e) {
    console.warn('Falha ao carregar produto (fallback):', e);
  }
}

function fill(p) {
  if (!$nome.value) $nome.value = p.nome ?? '';
  if (!$descricao.value) $descricao.value = p.descricao ?? '';
  if (!$preco.value) $preco.value = p.preco ?? p.precoVenda ?? '';
  if (!$imagem.value) $imagem.value = p.imagem ?? p.imagemUrl ?? '';
  if ($alertaEstoqueLimite && !$alertaEstoqueLimite.value) {
    $alertaEstoqueLimite.value = p.alertaEstoqueLimite ?? '';
  }
  if (!$categoria.value) $categoria.value = p.categoria ?? '';
  if (!$estoque.value) $estoque.value = p.estoque ?? p.estoqueAtual ?? 0;
  if (!$codigoBarras.value) $codigoBarras.value = p.codigoBarras ?? '';
  if ($disponivel) $disponivel.checked = (p.situacao ? p.situacao === 'ATIVO' : !!p.disponivel);
  syncGalleryState(resolveGalleryImages(p), p.imagem ?? p.imagemUrl ?? $imagem.value);
  syncPerfilVendaFromLimite();
}

function imagePlaceholderUrl() {
  return '/img/produtos/placeholder-generico.png';
}

function updateImagePreview(url) {
  if (!$imagemPreview) return;
  const value = String(url || '').trim();
  $imagemPreview.src = value || imagePlaceholderUrl();
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, (char) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  })[char]);
}

function normalizeGalleryUrl(url) {
  const value = String(url || '').trim();
  return value || null;
}

function dedupeGalleryImages(images) {
  const unique = new Set();
  return (Array.isArray(images) ? images : [])
    .map(normalizeGalleryUrl)
    .filter((image) => {
      if (!image || unique.has(image)) {
        return false;
      }
      unique.add(image);
      return true;
    });
}

function resolveGalleryImages(payload) {
  const primary = normalizeGalleryUrl(payload?.imagem ?? payload?.imagemUrl);
  const images = Array.isArray(payload?.imagens) ? payload.imagens : [];
  return dedupeGalleryImages(primary ? [primary, ...images] : images);
}

function loadInitialGalleryImages() {
  const images = Array.from(document.querySelectorAll('[data-gallery-image]'))
    .map((input) => normalizeGalleryUrl(input.value));
  const primary = normalizeGalleryUrl($imagem?.value);
  return dedupeGalleryImages(primary ? [primary, ...images] : images);
}

function syncGalleryState(images, preferredPrimary) {
  const normalizedImages = dedupeGalleryImages(images);
  const primary = normalizeGalleryUrl(preferredPrimary);
  if (primary && normalizedImages.includes(primary)) {
    normalizedImages.splice(normalizedImages.indexOf(primary), 1);
    normalizedImages.unshift(primary);
  }
  galleryImages = normalizedImages;
  if ($imagem) {
    $imagem.value = galleryImages[0] || '';
  }
  updateImagePreview(galleryImages[0] || imagePlaceholderUrl());
  renderImageGallery();
}

function buildGalleryPayload() {
  const primary = normalizeGalleryUrl($imagem?.value);
  return dedupeGalleryImages(primary ? [primary, ...galleryImages] : galleryImages);
}

function syncGalleryFromPrimaryInput() {
  const payload = buildGalleryPayload();
  syncGalleryState(payload, payload[0] || null);
}

function renderImageGallery() {
  if (!$imagemGaleria) return;
  if (!galleryImages.length) {
    $imagemGaleria.innerHTML = '<p class="hint">Nenhuma imagem adicional enviada ainda.</p>';
    return;
  }

  $imagemGaleria.innerHTML = galleryImages.map((image, index) => `
    <article class="image-gallery-item${index === 0 ? ' is-primary' : ''}">
      <button type="button"
              class="image-gallery-thumb"
              data-gallery-primary="${escapeHtml(image)}"
              aria-pressed="${index === 0}">
        <img src="${escapeHtml(image)}"
             alt="Imagem ${index + 1}"
             loading="lazy"
             onerror="this.src='${imagePlaceholderUrl()}'" />
        <span class="image-gallery-badge">${index === 0 ? 'Principal' : 'Tornar principal'}</span>
      </button>
      <button type="button"
              class="btn btn-ghost image-gallery-remove"
              data-gallery-remove="${escapeHtml(image)}">
        Remover
      </button>
    </article>
  `).join('');
}

function clearOtherImageInputs(activeInput) {
  imageInputs.forEach((input) => {
    if (input && input !== activeInput) {
      input.value = '';
    }
  });
}

function resolveSelectedUploadFile() {
  return $imagemArquivoCamera?.files?.[0] || $imagemArquivo?.files?.[0] || null;
}

function updatePreviewFromFile(file) {
  if (!(file instanceof File)) {
    updateImagePreview($imagem?.value);
    return;
  }
  updateImagePreview(URL.createObjectURL(file));
}

function syncActionButtons() {
  const locked = isSaving || isWorkflowActionInProgress || imageUploadInProgress;
  if ($btnExcluir) $btnExcluir.disabled = locked;
  if ($btnExcluirSecundario) $btnExcluirSecundario.disabled = locked;
  if ($btnSalvar) $btnSalvar.disabled = locked;
  if ($btnPublicar) $btnPublicar.disabled = locked;
  if ($btnValidar) $btnValidar.disabled = locked;
  if ($btnUploadImagem) $btnUploadImagem.disabled = locked;
  if ($btnGerarIA) $btnGerarIA.disabled = locked;
  imageInputs.forEach((input) => {
    input.disabled = locked;
  });
}

function validateBeforeSave() {
  const nome = String($nome?.value || '').trim();
  if (!nome) return 'Nome e obrigatorio.';

  const categoria = String($categoria?.value || '').trim();
  if (!categoria) return 'Categoria e obrigatoria.';

  const preco = parseFloat(String($preco?.value || '').replace(',', '.'));
  if (!Number.isFinite(preco) || preco <= 0) {
    return 'Preco de venda deve ser maior que zero.';
  }

  const estoque = parseInt(String($estoque?.value || '').trim(), 10);
  if (!Number.isInteger(estoque) || estoque < 0) {
    return 'Estoque deve ser um numero inteiro maior ou igual a zero.';
  }

  const codigo = String($codigoBarras?.value || '').trim();
  if (codigo && !/^(\d{8}|\d{12,14})$/.test(codigo)) {
    return 'Codigo de barras deve ter 8 ou 12 a 14 digitos.';
  }

  const imagem = String($imagem?.value || '').trim();
  if (imagem) {
    if (imagem.length > 200) {
      return 'URL da imagem deve ter no maximo 200 caracteres.';
    }
    if (!/^(https?:\/\/|\/)/i.test(imagem)) {
      return 'URL da imagem deve iniciar com http(s):// ou /.';
    }
  }

  const alerta = String($alertaEstoqueLimite?.value || '').trim();
  if (alerta) {
    const parsed = parsePositiveInt(alerta);
    if (parsed == null || parsed < ALERTA_ESTOQUE_MIN || parsed > ALERTA_ESTOQUE_MAX) {
      return `Limite de alerta deve ficar entre ${ALERTA_ESTOQUE_MIN} e ${ALERTA_ESTOQUE_MAX}.`;
    }
  }

  const fiscalPatterns = [
    [$fiscalNcm, /^\d{8}$/, 'NCM deve ter 8 digitos.'],
    [$fiscalCest, /^\d{7}$/, 'CEST deve ter 7 digitos.'],
    [$fiscalCfop, /^\d{4}$/, 'CFOP deve ter 4 digitos.'],
    [$fiscalIcmsCst, /^\d{2,3}$/, 'ICMS CST deve ter 2 ou 3 digitos.'],
    [$fiscalCsosn, /^\d{3}$/, 'CSOSN deve ter 3 digitos.'],
    [$fiscalPisCst, /^\d{2}$/, 'PIS CST deve ter 2 digitos.'],
    [$fiscalCofinsCst, /^\d{2}$/, 'COFINS CST deve ter 2 digitos.']
  ];

  for (const [field, pattern, message] of fiscalPatterns) {
    const value = String(field?.value || '').trim();
    if (value && !pattern.test(value)) {
      return message;
    }
  }

  const origem = String($fiscalOrigem?.value || '').trim();
  if (origem && !/^[0-8]$/.test(origem)) {
    return 'Origem fiscal deve estar entre 0 e 8.';
  }

  return null;
}

function syncTarjaReceitaRule() {
  const tarja = document.getElementById('tarjaMedicacao');
  const receita = document.getElementById('exigeReceita');
  if (tarja) {
    tarja.value = '';
    tarja.disabled = true;
  }
  if (receita) {
    receita.checked = false;
    receita.disabled = true;
  }
}

function parsePositiveInt(raw) {
  const parsed = parseInt(String(raw ?? '').trim(), 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

function clampAlertaLimite(value) {
  if (!Number.isFinite(value)) return ALERTA_ESTOQUE_PADRAO;
  return Math.min(ALERTA_ESTOQUE_MAX, Math.max(ALERTA_ESTOQUE_MIN, value));
}

function resolveLimiteGlobal() {
  const fromDataAttr = parsePositiveInt($alertaEstoqueLimite?.dataset?.globalLimite);
  return clampAlertaLimite(fromDataAttr ?? ALERTA_ESTOQUE_PADRAO);
}

function resolveLimitePorPerfil(perfil) {
  const multiplier = PERFIL_VENDA_MULTIPLIERS[perfil];
  if (!multiplier) return null;
  return clampAlertaLimite(Math.round(resolveLimiteGlobal() * multiplier));
}

function resolvePerfilPeloLimite(limiteAtual) {
  if (limiteAtual == null) return 'NORMAL';
  for (const perfil of Object.keys(PERFIL_VENDA_MULTIPLIERS)) {
    if (limiteAtual === resolveLimitePorPerfil(perfil)) {
      return perfil;
    }
  }
  return 'PERSONALIZADO';
}

function resolvePerfilHint(perfil, limiteAtual) {
  const limiteGlobal = resolveLimiteGlobal();
  switch (perfil) {
    case 'LENTA':
      return `Venda lenta: limite sugerido ${resolveLimitePorPerfil('LENTA')} (global ${limiteGlobal}).`;
    case 'NORMAL':
      return `Venda normal: limite sugerido ${resolveLimitePorPerfil('NORMAL')} (global ${limiteGlobal}).`;
    case 'RAPIDA':
      return `Venda rapida: limite sugerido ${resolveLimitePorPerfil('RAPIDA')} (global ${limiteGlobal}).`;
    default:
      if (limiteAtual == null) {
        return `Personalizado: em branco usa o limite global (${limiteGlobal}).`;
      }
      return `Personalizado: limite manual atual ${limiteAtual}.`;
  }
}

function applyPerfilHint(perfil, limiteAtual) {
  if (!$perfilVendaHint) return;
  $perfilVendaHint.textContent = resolvePerfilHint(perfil, limiteAtual);
}

function syncPerfilVendaFromLimite() {
  if (!$perfilVenda || !$alertaEstoqueLimite) return;
  const limiteAtual = parsePositiveInt($alertaEstoqueLimite.value);
  const perfil = resolvePerfilPeloLimite(limiteAtual);
  $perfilVenda.value = perfil;
  applyPerfilHint(perfil, limiteAtual);
}

function onPerfilVendaChange() {
  if (!$perfilVenda || !$alertaEstoqueLimite) return;
  const perfil = String($perfilVenda.value || 'PERSONALIZADO').toUpperCase();
  if (perfil === 'PERSONALIZADO') {
    applyPerfilHint(perfil, parsePositiveInt($alertaEstoqueLimite.value));
    return;
  }

  const limite = resolveLimitePorPerfil(perfil);
  if (limite != null) {
    $alertaEstoqueLimite.value = String(limite);
  }
  applyPerfilHint(perfil, limite);
}

function onAlertaLimiteInput() {
  if (!$perfilVenda || !$alertaEstoqueLimite) return;
  const limiteAtual = parsePositiveInt($alertaEstoqueLimite.value);
  const perfil = resolvePerfilPeloLimite(limiteAtual);
  $perfilVenda.value = perfil;
  applyPerfilHint(perfil, limiteAtual);
}

function normalizeAlertaLimite() {
  if (!$alertaEstoqueLimite) return;
  const parsed = parsePositiveInt($alertaEstoqueLimite.value);
  if (parsed == null) {
    $alertaEstoqueLimite.value = '';
  } else {
    $alertaEstoqueLimite.value = String(clampAlertaLimite(parsed));
  }
  onAlertaLimiteInput();
}

async function salvar() {
  const id = resolveProdutoId($id?.value);
  if (!id) return toast('ID do produto ausente.', 'err');
  if (isSaving || isWorkflowActionInProgress || imageUploadInProgress) {
    toast('Aguarde a acao atual terminar para salvar.', 'err');
    return;
  }

  const validationMessage = validateBeforeSave();
  if (validationMessage) {
    $status.textContent = validationMessage;
    toast(validationMessage, 'err');
    return;
  }

  const imagens = buildGalleryPayload();
  syncGalleryState(imagens, imagens[0] || null);
  const precoValue = parseFloat(String($preco.value || '0').replace(',', '.'));
  const estoqueValue = parseInt(String($estoque.value || '0').trim(), 10);
  const dto = {
    nome: $nome.value || null,
    descricao: $descricao.value || null,
    preco: precoValue,
    imagem: imagens[0] || null,
    imagens,
    categoria: $categoria.value || null,
    tarjaMedicacao: null,
    exigeReceita: false,
    estoque: estoqueValue,
    alertaEstoqueLimite: (() => {
      if (!$alertaEstoqueLimite) return null;
      const parsed = parsePositiveInt($alertaEstoqueLimite.value);
      return parsed == null ? null : clampAlertaLimite(parsed);
    })(),
    codigoBarras: $codigoBarras.value || null,
    fiscalNcm: $fiscalNcm?.value || null,
    fiscalCest: $fiscalCest?.value || null,
    fiscalCfop: $fiscalCfop?.value || null,
    fiscalOrigem: (() => {
      const raw = String($fiscalOrigem?.value || '').trim();
      return raw ? Number(raw) : null;
    })(),
    fiscalIcmsCst: $fiscalIcmsCst?.value || null,
    fiscalCsosn: $fiscalCsosn?.value || null,
    fiscalPisCst: $fiscalPisCst?.value || null,
    fiscalCofinsCst: $fiscalCofinsCst?.value || null,
    ativo: !!$disponivel.checked
  };

  isSaving = true;
  syncActionButtons();
  $status.textContent = 'Salvando...';

  try {
    const r = await fetch(`/api/admin/produtos/${id}`, withCsrf({
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dto),
      credentials: 'same-origin'
    }));

    if (!r.ok) {
      const raw = await r.text();
      const contentType = String(r.headers.get('content-type') || '').toLowerCase();
      if (isSessionRedirect(r, contentType, raw)) {
        throw new Error('Sessao expirada. Faca login novamente.');
      }
      throw new Error(parseApiErrorMessage(raw, `Falha ao salvar (HTTP ${r.status})`));
    }
    $status.textContent = 'Salvo com sucesso.';
    toast('Produto salvo!', 'ok');
  } catch (e) {
    console.error(e);
    $status.textContent = 'Erro ao salvar.';
    toast(e?.message || 'Erro ao salvar produto', 'err');
  } finally {
    isSaving = false;
    syncActionButtons();
  }
}
async function uploadImagem(files) {
  if (imageUploadInProgress) return;
  if (isSaving || isWorkflowActionInProgress) {
    toast('Aguarde a acao atual terminar para enviar imagem.', 'err');
    return;
  }

  const id = resolveProdutoId($id?.value);
  if (!id) return toast('Produto ainda nao salvo.', 'err');

  const selectedFiles = Array.from(files || []).filter((file) => file instanceof File);
  if (!selectedFiles.length) {
    $imagemArquivoCamera?.click();
    return;
  }

  for (const file of selectedFiles) {
    const contentType = String(file.type || '').toLowerCase();
    if (contentType && !ALLOWED_UPLOAD_IMAGE_TYPES.has(contentType)) {
      toast('Formato invalido. Use JPG, PNG ou WEBP.', 'err');
      return;
    }
    if (Number(file.size || 0) > MAX_UPLOAD_IMAGE_BYTES) {
      toast('Imagem acima de 20MB. Reduza o arquivo e tente novamente.', 'err');
      return;
    }
  }

  const formData = new FormData();
  selectedFiles.forEach((file) => formData.append('file', file));

  imageUploadInProgress = true;
  syncActionButtons();
  $status.textContent = selectedFiles.length > 1
    ? `Enviando ${selectedFiles.length} imagens...`
    : 'Enviando imagem...';

  try {
    const endpoint = `/api/admin/produtos/${id}/imagens`;
    const createRequest = () => withCsrf({
      method: 'POST',
      body: formData,
      credentials: 'same-origin'
    });
    let r = await fetch(endpoint, createRequest());

    if (!r.ok && isRetryableUploadStatus(r.status)) {
      await new Promise((resolve) => setTimeout(resolve, 450));
      r = await fetch(endpoint, createRequest());
    }

    if (!r.ok) {
      throw new Error(await responseToUploadError(r, `Falha no upload (HTTP ${r.status})`));
    }
    const payload = await r.json();
    syncGalleryState(resolveGalleryImages(payload), payload?.imagem ?? payload?.imagemUrl);
    $status.textContent = selectedFiles.length > 1 ? 'Imagens enviadas.' : 'Imagem enviada.';
    toast(selectedFiles.length > 1 ? 'Imagens atualizadas!' : 'Imagem atualizada!', 'ok');
  } catch (e) {
    console.error(e);
    $status.textContent = 'Erro ao enviar imagem.';
    updateImagePreview($imagem?.value);
    const msg = String(e?.message || '').trim();
    const lower = msg.toLowerCase();
    if (/401|403|unauthorized|forbidden|full authentication/i.test(lower)) {
      toast('Sua sessao expirou. Faca login novamente e tente o upload.', 'err');
    } else if (/413|payload too large|payload_too_large|arquivo muito grande/i.test(lower)) {
      toast('Imagem acima de 20MB. Reduza o arquivo e tente novamente.', 'err');
    } else if (/502|503|504|backend write error|service unavailable|varnish/i.test(lower)) {
      toast('Falha de upload no servidor. Use JPG/PNG/WEBP com ate 20MB e tente novamente.', 'err');
    } else if (/failed to fetch|networkerror|network request failed/i.test(lower)) {
      toast('Falha de rede no upload. Tente novamente em alguns segundos.', 'err');
    } else {
      toast(msg || 'Falha ao enviar imagem', 'err');
    }
  } finally {
    imageUploadInProgress = false;
    syncActionButtons();
  }
}
async function gerarIA() {
  const id = resolveProdutoId($id?.value);
  if (!id) return toast('ID do produto invalido.', 'err');
  if (isSaving || isWorkflowActionInProgress || imageUploadInProgress) {
    toast('Aguarde a acao atual terminar para solicitar imagem IA.', 'err');
    return;
  }

  const hasCurrentImage = String($imagem?.value || '').trim().length > 0;
  const endpoint = hasCurrentImage
    ? `/admin/imagens/${id}/regenerate`
    : `/admin/imagens/${id}/queue`;

  isWorkflowActionInProgress = true;
  syncActionButtons();
  $status.textContent = hasCurrentImage
    ? 'Solicitando regeneracao de imagem...'
    : 'Solicitando geracao de imagem...';

  try {
    const r = await fetch(endpoint, withCsrf({ method: 'POST', credentials: 'same-origin' }));
    const raw = await r.text();
    const payload = parseJsonSafely(raw);

    if (!r.ok) {
      throw new Error(
        normalizeErrorMessage(
          payload?.lastJob?.errorMsg || payload?.message || raw,
          `Falha na solicitacao de imagem (HTTP ${r.status})`
        )
      );
    }

    const lastJobStatus = String(payload?.lastJob?.status || '');
    const lastJobError = payload?.lastJob?.errorMsg || '';
    if (lastJobStatus === 'ERROR') {
      throw new Error(
        normalizeErrorMessage(lastJobError, 'A IA nao conseguiu gerar a imagem para esse produto.')
      );
    }

    const resultUrl = String(payload?.lastJob?.resultUrl || '').trim();
    if (resultUrl) {
      $imagem.value = resultUrl;
      updateImagePreview(resultUrl);
    }

    const result = String(payload?.result || '');
    if (
      lastJobStatus === 'DONE'
      || result.startsWith('PROCESSADO_SYNC')
      || result === 'REGERADO'
      || result === 'PROCESSADO_SYNC_IMAGEM_EXISTENTE'
    ) {
      toast('Imagem gerada e salva!', 'ok');
      $status.textContent = 'Imagem gerada e salva.';
    } else {
      toast(hasCurrentImage ? 'Regeneracao solicitada!' : 'Enfileirado com sucesso!', 'ok');
      $status.textContent = hasCurrentImage
        ? 'Regeneracao enfileirada...'
        : 'Geracao enfileirada...';
    }
  } catch (e) {
    console.error(e);
    $status.textContent = 'Erro ao solicitar imagem.';
    toast(e?.message || 'Falha na solicitacao de imagem', 'err');
  } finally {
    isWorkflowActionInProgress = false;
    syncActionButtons();
  }
}
function validadorAtual() {
  const nome = ($validador?.value || '').trim();
  return nome || 'admin-web';
}

async function moverFluxo(acao) {
  const id = resolveProdutoId($id?.value);
  if (!id) return toast('Produto ainda nao salvo.', 'err');
  if (isSaving || imageUploadInProgress || isWorkflowActionInProgress) {
    toast('Aguarde a acao atual terminar antes de mudar o fluxo.', 'err');
    return;
  }

  const validador = encodeURIComponent(validadorAtual());
  const endpoint = `/admin/produtos/${id}/${acao}?validador=${validador}`;
  const acaoLabel = acao === 'validar' ? 'validando' : 'publicando';

  isWorkflowActionInProgress = true;
  syncActionButtons();
  $status.textContent = `Fluxo: ${acaoLabel}...`;

  try {
    const r = await fetch(endpoint, withCsrf({ method: 'POST', credentials: 'same-origin' }));
    if (!r.ok) {
      const raw = await r.text();
      const contentType = String(r.headers.get('content-type') || '').toLowerCase();
      if (isSessionRedirect(r, contentType, raw)) {
        throw new Error('Sessao expirada. Faca login novamente.');
      }
      throw new Error(parseApiErrorMessage(raw, `Falha no fluxo (HTTP ${r.status})`));
    }

    if (acao === 'validar') {
      $status.textContent = 'Fluxo: produto VALIDADO.';
      toast('Produto validado!', 'ok');
    } else {
      $status.textContent = 'Fluxo: produto PUBLICADO.';
      toast('Produto publicado!', 'ok');
    }
  } catch (e) {
    console.error(e);
    $status.textContent = `Erro ao ${acao}.`;
    toast(e?.message || `Falha ao ${acao} produto`, 'err');
  } finally {
    isWorkflowActionInProgress = false;
    syncActionButtons();
  }
}
async function validar() {
  await moverFluxo('validar');
}

async function publicar() {
  await moverFluxo('publicar');
}

// Listeners
$btnSalvar?.addEventListener('click', salvar);
$btnGerarIA?.addEventListener('click', gerarIA);
$btnValidar?.addEventListener('click', validar);
$btnPublicar?.addEventListener('click', publicar);
$categoria?.addEventListener('change', syncTarjaReceitaRule);
$perfilVenda?.addEventListener('change', onPerfilVendaChange);
$alertaEstoqueLimite?.addEventListener('input', onAlertaLimiteInput);
$alertaEstoqueLimite?.addEventListener('blur', normalizeAlertaLimite);
$imagem?.addEventListener('input', () => updateImagePreview($imagem.value));
$imagem?.addEventListener('blur', syncGalleryFromPrimaryInput);
$imagemGaleria?.addEventListener('click', (event) => {
  const removeButton = event.target.closest('[data-gallery-remove]');
  if (removeButton) {
    const image = normalizeGalleryUrl(removeButton.getAttribute('data-gallery-remove'));
    galleryImages = galleryImages.filter((item) => item !== image);
    syncGalleryState(galleryImages, galleryImages[0] || null);
    return;
  }

  const primaryButton = event.target.closest('[data-gallery-primary]');
  if (primaryButton) {
    const image = normalizeGalleryUrl(primaryButton.getAttribute('data-gallery-primary'));
    syncGalleryState(galleryImages, image);
  }
});
imageInputs.forEach((input) => {
  input.addEventListener('change', async () => {
    const files = Array.from(input?.files || []);
    if (!files.length) {
      return;
    }
    clearOtherImageInputs(input);
    updatePreviewFromFile(files[0]);
    await uploadImagem(files);
    input.value = '';
  });
});

// bootstrap
syncTarjaReceitaRule();
syncPerfilVendaFromLimite();
syncGalleryState(loadInitialGalleryImages(), $imagem?.value);
carregarProdutoSeNecessario();
updateImagePreview($imagem?.value);
syncActionButtons();
