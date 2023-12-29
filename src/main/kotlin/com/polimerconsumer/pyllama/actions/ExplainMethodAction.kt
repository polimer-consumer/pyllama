package com.polimerconsumer.pyllama.actions

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyFunction
import com.polimerconsumer.pyllama.actions.services.APICallService

class ExplainMethodAction : AnAction() {
    override fun update(actionEvent: AnActionEvent) {
        val project: Project? = actionEvent.project
        actionEvent.presentation.isEnabledAndVisible = project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val editor = actionEvent.getData(CommonDataKeys.EDITOR)
        val project = actionEvent.getData(CommonDataKeys.PROJECT)

        if (editor != null && project != null) {
            val selectedMethod = findMethodAtCaret(editor, project)

            if (selectedMethod != null) {
                val explanation = service<APICallService>().callChatGPTAPI(selectedMethod.text)

                showExplanationDialog(project, explanation)
            }
        }
    }

    private fun showExplanationDialog(project: Project, explanation: String) {
        com.intellij.openapi.ui.Messages.showMessageDialog(
            project,
            explanation,
            "Method Explanation",
            com.intellij.openapi.ui.Messages.getInformationIcon()
        )
    }

    private fun findMethodAtCaret(editor: Editor, project: Project): PyFunction? {
        val offset = editor.caretModel.offset
        val psiFile = findPsiFile(editor, project)

        return if (psiFile != null) {
            findMethodAtOffset(psiFile, offset)
        } else {
            null
        }
    }

    private fun findPsiFile(editor: Editor, project: Project): PsiFile? {
        val file = editor.virtualFile
        return com.intellij.psi.PsiManager.getInstance(project).findFile(file)
    }

    private fun findMethodAtOffset(psiFile: PsiFile, offset: Int): PyFunction? {
        val elementAtCaret = psiFile.findElementAt(offset)
        return PsiTreeUtil.getParentOfType(elementAtCaret, PyFunction::class.java)
    }
}
