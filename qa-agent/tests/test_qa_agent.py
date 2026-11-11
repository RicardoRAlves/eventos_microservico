import io
import json
import os
import sys
import tempfile
import unittest
from pathlib import Path
from urllib.error import HTTPError
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import qa_agent as qa


class FakeResponse:
    def __init__(self, status=401, body=b""):
        self.status, self.body = status, body
    def __enter__(self):
        return self
    def __exit__(self, *_):
        return False
    def read(self, _):
        return self.body


class FakeOpener:
    def __init__(self):
        self.calls = []
    def open(self, req, **kwargs):
        self.calls.append((req, kwargs))
        return FakeResponse()


def proposal(reference_id="EP-EVT-006", **changes):
    return {"reference_id": reference_id, "scenario": "Verificar leitura",
            "preconditions": "Ambiente isolado", "expected_evidence": "Status HTTP"} | changes


class FakeOllamaOpener:
    def __init__(self, *answers):
        self.answers = iter(answers)
        self.calls = []

    def open(self, req, **kwargs):
        self.calls.append((req, json.loads(req.data)))
        answer = next(self.answers)
        if isinstance(answer, Exception):
            raise answer
        content = json.dumps({"proposals": answer}) if isinstance(answer, list) else answer
        body = json.dumps({"message": {"content": content}}).encode("utf-8")
        return FakeResponse(200, body)


class AgentTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.original_repo, cls.original_docs = qa.REPO, qa.DOCS
        # No repositório instalado, o padrão já aponta para a documentação real.
        if os.getenv("QA_REPO_FOR_TESTS"):
            qa.REPO = Path(os.environ["QA_REPO_FOR_TESTS"])
            qa.DOCS = qa.REPO / "docs" / "qa-agent-knowledge"

    @classmethod
    def tearDownClass(cls):
        qa.REPO, qa.DOCS = cls.original_repo, cls.original_docs

    def test_contracts_and_no_side_effect_get(self):
        data, hashes = qa.load_knowledge()
        self.assertEqual((len(data["endpoints"]), len(data["messages"]), len(data["tests"])), (47, 19, 41))
        self.assertGreater(len(hashes), 20)
        cases = qa.approved_cases(data)
        self.assertEqual(len(cases), 3)
        with self.assertRaises(qa.ConfigError):
            qa.safe_read(data["endpoints"]["EP-EVT-001"])

    def test_paths_and_targets_block_escape(self):
        self.assertFalse(qa.exact_path("/x/{id}", "/x/../delete", [{"name":"id","type":"Long"}]))
        self.assertFalse(qa.exact_path("/x/{id}", "/x/1?foo=bar", [{"name":"id","type":"Long"}]))
        self.assertFalse(qa.exact_path("/x/{id}", "http://example.com", [{"name":"id","type":"Long"}]))
        for url in ("https://127.0.0.1:8080", "http://example.com:8080", "http://127.0.0.1:8080/x", "http://127.0.0.1:8080@evil.com"):
            with self.assertRaises(qa.ConfigError):
                qa.validate_base(url)

    def test_modified_catalog_blocks_execution(self):
        data, _ = qa.load_knowledge()
        data["endpoints"]["EP-EVT-006"]["messages"] = ["MSG-EVT-GET-ALL"]
        with self.assertRaises(qa.ConfigError):
            qa.approved_cases(data)

    def test_http_only_get_without_auth(self):
        fake = FakeOpener()
        with patch.object(qa, "opener", return_value=fake):
            status, _, err, _ = qa.http_get("http://127.0.0.1:8080/api/v1/categoria/all")
        self.assertEqual((status, err), (401, None))
        req, kwargs = fake.calls[0]
        self.assertEqual(req.get_method(), "GET")
        self.assertNotIn("Authorization", req.headers)
        self.assertEqual(kwargs["timeout"], 8)

    def test_async_poll_and_classification(self):
        data, _ = qa.load_knowledge()
        obs = {"id":"projection", "endpoint_id":"EP-USR-021", "path":"/api/v1/users/event-reservations/count/users/event/sale/123",
               "json_pointer":"/count", "equals":1, "timeout_seconds":2}
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "observations.json"
            path.write_text(json.dumps({"observations":[obs]}), encoding="utf-8")
            qa.observations(path, data)
        with patch.object(qa, "http_get", side_effect=[(200,{"count":0},None,1),(200,{"count":1},None,1)]), patch.object(qa.time, "sleep"):
            rows = qa.observe([obs], data, {"user_api":"http://127.0.0.1:8082"})
        self.assertEqual(rows[0]["result"], "PASSOU")
        self.assertIn("2 leituras", rows[0]["evidence"])

    def test_proposals_are_never_executed(self):
        fake = FakeOllamaOpener([proposal("EP-EVT-001"), proposal()])
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            approved = root / "approved-tests.json"
            approved.write_bytes((qa.ROOT / approved.name).read_bytes())
            original = approved.read_bytes()
            with patch.object(qa, "ROOT", root), patch.object(qa, "opener", return_value=fake), \
                 patch.object(qa, "http_get") as http_get, patch("sys.stderr", new_callable=io.StringIO):
                self.assertEqual(qa.main(["propose"]), 0)
                http_get.assert_not_called()
            result = qa.read_json(root / "proposals.json")["proposals"]
            self.assertEqual([p["reference_id"] for p in result], ["EP-EVT-001", "EP-EVT-006"])
            self.assertTrue(all(p["readiness"] == "REVIEW_REQUIRED" for p in result))
            self.assertEqual(approved.read_bytes(), original)
            self.assertEqual(len(fake.calls), 1)
            self.assertTrue(fake.calls[0][0].full_url.endswith("/api/chat"))

    def test_report_creates_fixed_pdf_from_stored_proposals_without_http(self):
        document = {
            "proposals": [proposal() | {"readiness": "REVIEW_REQUIRED"}],
            "documentation_sha256": {"api/endpoints.json": "abc"},
        }
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "approved-tests.json").write_bytes((qa.ROOT / "approved-tests.json").read_bytes())
            (root / "proposals.json").write_text(
                json.dumps(document, ensure_ascii=False), encoding="utf-8")
            reports = root / "reports"
            with patch.object(qa, "ROOT", root), patch.object(qa, "http_get") as http_get:
                self.assertEqual(qa.main(["report", "--reports", str(reports)]), 0)
                http_get.assert_not_called()
            pdf = reports / "qa-report.pdf"
            content = pdf.read_bytes()
            self.assertGreater(len(content), 5_000)
            self.assertTrue(content.startswith(b"%PDF-"))
            self.assertTrue(content.rstrip().endswith(b"%%EOF"))

    def test_report_uses_only_stored_generation_metadata(self):
        model, metrics = qa.proposal_report_metadata({"proposals": []})
        self.assertEqual(model, "Não disponível")
        self.assertTrue(all(value == "Não disponível" for _, value in metrics))
        model, metrics = qa.proposal_report_metadata({
            "proposals": [],
            "generation": {"model": "modelo-real", "eval_count": 42},
        })
        self.assertEqual(model, "modelo-real")
        self.assertIn(("Tokens gerados", "42"), metrics)

    def test_report_preserves_utf8_and_missing_fields(self):
        with tempfile.TemporaryDirectory() as tmp:
            output = Path(tmp) / "relatório.pdf"
            result = qa.write_proposal_report(
                {"proposals": [{"reference_id": "EP-EVT-006", "scenario": "Ação válida"}]},
                {"endpoints": {}, "messages": {}}, output)
            self.assertEqual(result, output)
            self.assertTrue(output.is_file())
        self.assertEqual(qa.available(""), "Não disponível")
        self.assertEqual(qa.available("pré-condição"), "pré-condição")

    def run_workspace(self, root, http_results, proposals=None, diagnosis=None):
        (root / "approved-tests.json").write_bytes((qa.ROOT / "approved-tests.json").read_bytes())
        (root / "environment.example.json").write_bytes(
            (qa.ROOT / "environment.example.json").read_bytes())
        proposals = proposals if proposals is not None else [proposal() | {"readiness": "REVIEW_REQUIRED"}]
        diagnosis = diagnosis if diagnosis is not None else {
            "summary": "Resultados objetivos preservados.",
            "hypotheses": ["Verificar a configuração local."],
            "infrastructure_failures": [],
        }
        captured = {}
        real_writer = qa.write_autonomous_report

        def capture_report(report, output):
            captured.update(report)
            return real_writer(report, output)

        with patch.object(qa, "ROOT", root), \
             patch.object(qa, "ollama_proposals", return_value=proposals), \
             patch.object(qa, "ollama_diagnosis", return_value=diagnosis), \
             patch.object(qa, "http_get", side_effect=http_results) as http_get, \
             patch.object(qa, "write_autonomous_report", side_effect=capture_report):
            code = qa.main(["run"])
        return code, captured, http_get

    def test_autonomous_run_executes_only_three_authorized_gets_and_creates_pdf(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            original = (qa.ROOT / "approved-tests.json").read_bytes()
            code, report, http_get = self.run_workspace(
                root, [(401, {"message": "Não autorizado"}, None, 2.5)] * 3,
                proposals=[proposal("EP-EVT-002", scenario="Criar evento") |
                           {"readiness": "REVIEW_REQUIRED"}],
            )
            self.assertEqual(code, 0)
            self.assertEqual(http_get.call_count, 3)
            urls = [call.args[0] for call in http_get.call_args_list]
            self.assertTrue(all(url.startswith("http://127.0.0.1:8080/api/v1/categoria/")
                                for url in urls))
            self.assertFalse(any("evento/create" in url for url in urls))
            self.assertTrue(all(row["http_method"] == "GET" for row in report["tests"]))
            self.assertTrue(all(row["result"] == "PASSOU" for row in report["tests"]))
            self.assertEqual(report["proposals"][0]["readiness"], "REVIEW_REQUIRED")
            pdf = root / "reports" / "qa-run-report.pdf"
            self.assertGreater(pdf.stat().st_size, 5_000)
            self.assertTrue(pdf.read_bytes().startswith(b"%PDF-"))
            self.assertEqual((root / "approved-tests.json").read_bytes(), original)

    def test_autonomous_run_records_http_and_transport_failures(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            code, report, _ = self.run_workspace(root, [
                (500, {"error": "falha"}, None, 4.2),
                (None, None, "URLError", 8.0),
                (401, {"message": "Não autorizado"}, None, 2.1),
            ])
            self.assertEqual(code, 1)
            self.assertEqual([row["result"] for row in report["tests"]],
                             ["INCONCLUSIVO", "ERRO", "PASSOU"])
            self.assertEqual(report["tests"][1]["execution_state"], "BLOQUEADO")
            self.assertEqual(report["tests"][0]["received"], 500)
            self.assertEqual(report["tests"][0]["observed_response"], '{"error":"falha"}')
            self.assertTrue((root / "reports" / "qa-run-report.pdf").is_file())

    def test_autonomous_run_generates_pdf_when_ollama_is_unavailable(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "approved-tests.json").write_bytes((qa.ROOT / "approved-tests.json").read_bytes())
            (root / "environment.example.json").write_bytes(
                (qa.ROOT / "environment.example.json").read_bytes())
            captured = {}
            real_writer = qa.write_autonomous_report

            def capture_report(report, output):
                captured.update(report)
                return real_writer(report, output)

            with patch.object(qa, "ROOT", root), \
                 patch.object(qa, "ollama_proposals", side_effect=qa.ConfigError("Ollama indisponível")), \
                 patch.object(qa, "ollama_diagnosis", side_effect=qa.ConfigError("Ollama indisponível")), \
                 patch.object(qa, "http_get", return_value=(401, {}, None, 1.0)) as http_get, \
                 patch.object(qa, "write_autonomous_report", side_effect=capture_report):
                code = qa.main(["run"])
            self.assertEqual(code, 1)
            self.assertEqual(http_get.call_count, 3)
            self.assertEqual(captured["proposal_status"], "INDISPONÍVEL")
            self.assertEqual(captured["diagnosis_status"], "INDISPONÍVEL")
            self.assertIn("Ollama indisponível", captured["proposal_error"])
            self.assertTrue((root / "reports" / "qa-run-report.pdf").is_file())

    def test_observed_responses_redact_secrets(self):
        observed = qa.observed_text({
            "token": "segredo",
            "nested": {"password": "senha", "message": "Bearer abc.def"},
        })
        self.assertNotIn("segredo", observed)
        self.assertNotIn("senha", observed)
        self.assertNotIn("abc.def", observed)
        self.assertIn("[REDACTED]", observed)

    def test_diagnosis_cannot_promote_unobserved_http_status_to_fact(self):
        warnings = qa.diagnosis_warnings(
            {"summary": "Todos os testes retornaram HTTP 401.", "hypotheses": [],
             "infrastructure_failures": []},
            [{"received": None, "result": "ERRO"}],
        )
        self.assertEqual(warnings,
                         ["A IA mencionou códigos HTTP não observados nesta execução: 401."])
        self.assertEqual(qa.diagnosis_warnings(
            {"summary": "Foi observado HTTP 401.", "hypotheses": [],
             "infrastructure_failures": []},
            [{"received": 401, "result": "PASSOU"}]), [])

    def test_observe_blocks_get_outside_approved_list(self):
        obs = {"endpoint_id": "EP-USR-021", "path": "/api/v1/users/event-reservations/count/users/event/sale/123",
               "json_pointer": "/count", "equals": 1, "timeout_seconds": 2}
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "observations.json"
            path.write_text(json.dumps({"observations": [obs]}), encoding="utf-8")
            with patch.object(qa, "http_get") as http_get, patch("sys.stderr", new_callable=io.StringIO) as logs:
                self.assertEqual(qa.main(["observe", "--observations", str(path)]), 2)
                http_get.assert_not_called()
            self.assertIn("fora dos GETs aprovados", logs.getvalue())


class OllamaTests(unittest.TestCase):
    def setUp(self):
        self.url = "http://127.0.0.1:11434"
        self.knowledge = {
            "endpoints": {"EP-EVT-006": {"service": "event_api", "http_method": "GET", "path": "/test"}},
            "messages": {"MSG-01": {"producer": "processor_api"}},
        }

    def generate(self, *answers):
        fake = FakeOllamaOpener(*answers)
        with patch.object(qa, "opener", return_value=fake), patch("sys.stderr", new_callable=io.StringIO) as logs:
            result = qa.ollama_proposals(self.knowledge, "local-model", self.url)
        return result, fake.calls, logs.getvalue()

    def test_valid_endpoint_and_message_with_enum_schema(self):
        result, calls, logs = self.generate([proposal(), proposal("MSG-01")])
        self.assertEqual(result, [p | {"readiness": "REVIEW_REQUIRED"} for p in [proposal(), proposal("MSG-01")]])
        self.assertEqual(len(calls), 1)
        fields = calls[0][1]["format"]["properties"]["proposals"]["items"]["properties"]
        self.assertEqual(fields["reference_id"]["enum"], ["EP-EVT-006", "MSG-01"])
        self.assertIn("recebidas=2 válidas=2", logs)

    def test_repair_preserves_valid_items_and_sends_only_failed_positions(self):
        original = proposal(scenario="Manter")
        broken = proposal("EP-INVENTADO", expected_evidence=5)
        del broken["preconditions"]
        result, calls, logs = self.generate([broken, original], [proposal("MSG-01")])
        self.assertEqual([p["scenario"] for p in result], ["Verificar leitura", "Manter"])
        correction = json.loads(calls[1][1]["messages"][1]["content"])["correcoes"]
        self.assertEqual(correction, [{"posicao": 1, "motivos": ["reference_id desconhecido",
                        "campo preconditions ausente", "campo expected_evidence inválido"],
                        "campos_aproveitaveis": {"scenario": "Verificar leitura"}}])
        self.assertIn("tentativas_recuperação=1", logs)
        self.assertNotIn("EP-INVENTADO", logs)

    def test_retries_invalid_json_and_empty_list(self):
        result, calls, logs = self.generate("{", [], [proposal()])
        self.assertEqual(len(result), 1)
        self.assertEqual(len(calls), 3)
        self.assertIn("JSON de propostas inválido", logs)
        self.assertIn("Lista de propostas vazia", logs)

    def test_rejects_proposals_beyond_ten(self):
        result, calls, logs = self.generate([proposal() for _ in range(11)])
        self.assertEqual(len(result), 10)
        self.assertEqual(len(calls), 1)
        self.assertIn("1 propostas excedentes", logs)

    def test_exhausted_recovery_returns_only_valid_proposals(self):
        broken = proposal(["segredo"], preconditions=" ", readiness="READY")
        result, calls, logs = self.generate([proposal(), broken], [broken], [broken])
        self.assertEqual(len(result), 1)
        self.assertEqual(len(calls), 3)
        self.assertIn("Proposta 2 rejeitada: campo reference_id inválido", logs)
        self.assertNotIn("segredo", logs)
        self.assertIn("tentativas_recuperação=2", logs)

    def test_persistent_invalid_id_returns_no_proposals(self):
        broken = [proposal("EP-INVENTADO")]
        result, calls, logs = self.generate(broken, broken, broken)
        self.assertEqual(result, [])
        self.assertEqual(len(calls), 3)
        self.assertIn("rejeitadas_finais=1", logs)

    def test_missing_replacement_retries_only_remaining_position(self):
        broken = proposal("EP-INVENTADO")
        result, calls, logs = self.generate([broken, proposal(), broken], [proposal("MSG-01")], [proposal()])
        self.assertEqual(len(result), 3)
        correction = json.loads(calls[2][1]["messages"][1]["content"])["correcoes"]
        self.assertEqual(len(correction), 1)
        self.assertEqual(correction[0]["posicao"], 3)
        self.assertEqual(correction[0]["motivos"], ["correção ausente na resposta"])

    def test_explicit_schema_incompatibility_falls_back_to_validated_json(self):
        response = HTTPError(self.url, 400, "bad request", {}, io.BytesIO(
            b'{"error":"json: cannot unmarshal object into Go struct field ChatRequest.format of type string"}'))
        result, calls, logs = self.generate(response, [proposal("inventado")], [proposal()])
        self.assertEqual(len(result), 1)
        self.assertEqual([call[1]["format"] for call in calls[1:]], ["json", "json"])
        self.assertIn("usando modo JSON", logs)


if __name__ == "__main__":
    unittest.main()
