SELECT section_data->>'estimateType' FROM tb_document_detail WHERE doc_id=11 AND section_key='design_estimate';
UPDATE tb_document_detail SET section_data = jsonb_set(section_data, '{estimateType}', '"TYPE_C"') WHERE doc_id=11 AND section_key='design_estimate';
SELECT section_data->>'estimateType' FROM tb_document_detail WHERE doc_id=11 AND section_key='design_estimate';
